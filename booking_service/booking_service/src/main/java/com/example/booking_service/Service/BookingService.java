package com.example.booking_service.Service;

import com.example.booking_service.Client.MovieClient;
import com.example.booking_service.Client.SeatClient;
import com.example.booking_service.Client.ShowtimeClient;
import com.example.booking_service.DTO.*;
import com.example.booking_service.Exception.BookingException;
import com.example.booking_service.Exception.ServiceCommunicationException;
import com.example.booking_service.Model.Booking;
import com.example.booking_service.Repository.BookingRepository;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class BookingService {

    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);
    private static final int BOOKING_LOCK_SECONDS = 600; // 10 phút

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private MovieClient movieClient;

    @Autowired
    private ShowtimeClient showtimeClient;

    @Autowired
    private SeatClient seatClient;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * Tạo một đơn đặt vé mới
     * Sử dụng Redis để khóa ghế tạm thời tránh đặt ghế trùng
     */
    @Transactional
    @CircuitBreaker(name = "bookingService", fallbackMethod = "createBookingFallback")
    public BookingResponseDTO createBooking(BookingRequestDTO requestDTO) {
        logger.info("Creating booking for showtime ID: {} and seat: {}",
                requestDTO.getShowtimeId(), requestDTO.getSeatNumber());

        try {
            // 1. Kiểm tra suất chiếu tồn tại bằng cách gọi đến ShowtimeService
            ShowtimeDTO showtime;
            try {
                showtime = showtimeClient.getShowtimeById(requestDTO.getShowtimeId());
            } catch (FeignException e) {
                logger.error("Failed to get showtime information", e);
                throw new ServiceCommunicationException("Failed to communicate with Showtime Service", e);
            }

            // 2. Kiểm tra suất chiếu còn hiệu lực
            if (showtime.getTime().isBefore(LocalDateTime.now())) {
                throw new BookingException("Showtime has already passed");
            }

            // 3. Kiểm tra ghế còn trống bằng cách gọi đến SeatService
            SeatDTO seat;
            try {
                seat = seatClient.getSeatByShowtimeAndNumber(
                        requestDTO.getShowtimeId(), requestDTO.getSeatNumber());

                if (seat == null) {
                    throw new BookingException("Seat not found");
                }

                if (seat.isReserved()) {
                    throw new BookingException("Seat is already reserved");
                }
            } catch (FeignException e) {
                logger.error("Failed to get seat information", e);
                throw new ServiceCommunicationException("Failed to communicate with Seat Service", e);
            }

            // 4. Khóa ghế bằng Redis để tránh race condition
            String lockKey = "seat:" + showtime.getId() + ":" + requestDTO.getSeatNumber();
            Boolean lockAcquired = redisTemplate.opsForValue()
                    .setIfAbsent(lockKey, "locked", BOOKING_LOCK_SECONDS, TimeUnit.SECONDS);

            if (Boolean.FALSE.equals(lockAcquired)) {
                // Nếu không lấy được lock, có nghĩa là ghế đang được đặt bởi người khác
                throw new BookingException("Seat is currently being booked by someone else");
            }

            try {
                // 5. Gọi đến SeatService để đặt ghế
                seat.setReserved(true);
                try {
                    seatClient.reserveSeat(seat);
                } catch (FeignException e) {
                    logger.error("Failed to reserve seat", e);
                    // Giải phóng khóa Redis nếu không đặt được ghế
                    redisTemplate.delete(lockKey);
                    throw new ServiceCommunicationException("Failed to reserve seat", e);
                }

                // 6. Tạo đối tượng Booking mới
                Booking booking = new Booking();
                booking.setShowtimeId(requestDTO.getShowtimeId());
                booking.setSeatNumber(requestDTO.getSeatNumber());
                booking.setCustomerName(requestDTO.getCustomerName());
                booking.setEmail(requestDTO.getEmail());
                booking.setPaid(false);

                // 7. Lưu booking vào database
                booking = bookingRepository.save(booking);

                // 8. Lấy thông tin phim từ MovieService
                MovieDTO movie;
                try {
                    movie = movieClient.getMovieById(showtime.getMovieId());
                } catch (FeignException e) {
                    logger.error("Failed to get movie information", e);
                    // Không cần rollback vì thông tin phim chỉ cần cho response
                    movie = new MovieDTO();
                    movie.setTitle("Unknown Movie");
                }

                // 9. Chuyển đổi thành DTO để trả về
                BookingResponseDTO responseDTO = new BookingResponseDTO();
                responseDTO.setId(booking.getId());
                responseDTO.setShowtimeId(showtime.getId());
                responseDTO.setSeatNumber(booking.getSeatNumber());
                responseDTO.setCustomerName(booking.getCustomerName());
                responseDTO.setEmail(booking.getEmail());
                responseDTO.setPaid(booking.isPaid());
                responseDTO.setMovieTitle(movie.getTitle());
                responseDTO.setTheater(showtime.getTheater());
                responseDTO.setShowtimeDate(showtime.getTime());
                responseDTO.setPaymentUrl("/api/payment/" + booking.getId());

                logger.info("Booking created successfully. Booking ID: {}", booking.getId());

                return responseDTO;
            } catch (Exception e) {
                // Nếu có lỗi, giải phóng khóa Redis
                redisTemplate.delete(lockKey);
                throw e;
            }
        } catch (BookingException | ServiceCommunicationException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error creating booking", e);
            throw new BookingException("Failed to create booking: " + e.getMessage(), e);
        }
    }

    /**
     * Phương thức fallback cho Circuit Breaker khi tạo booking
     */
    public BookingResponseDTO createBookingFallback(BookingRequestDTO requestDTO, Exception e) {
        logger.error("Circuit breaker triggered for createBooking", e);
        throw new ServiceCommunicationException("Service is currently unavailable. Please try again later.");
    }

    /**
     * Hoàn tất đặt vé sau khi thanh toán thành công
     */
    @Transactional
    public BookingResponseDTO completeBooking(Long bookingId) {
        logger.info("Completing booking ID: {}", bookingId);

        // Tìm booking trong database
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingException("Booking not found"));

        // Kiểm tra trạng thái đã thanh toán
        if (!booking.isPaid()) {
            throw new BookingException("Booking has not been paid");
        }

        try {
            // Lấy thông tin suất chiếu
            ShowtimeDTO showtime = showtimeClient.getShowtimeById(booking.getShowtimeId());

            // Lấy thông tin phim
            MovieDTO movie = movieClient.getMovieById(showtime.getMovieId());

            // Chuyển đổi thành DTO để trả về
            BookingResponseDTO responseDTO = new BookingResponseDTO();
            responseDTO.setId(booking.getId());
            responseDTO.setShowtimeId(booking.getShowtimeId());
            responseDTO.setSeatNumber(booking.getSeatNumber());
            responseDTO.setCustomerName(booking.getCustomerName());
            responseDTO.setEmail(booking.getEmail());
            responseDTO.setPaid(booking.isPaid());
            responseDTO.setMovieTitle(movie.getTitle());
            responseDTO.setTheater(showtime.getTheater());
            responseDTO.setShowtimeDate(showtime.getTime());

            // Gửi email xác nhận
            emailService.sendBookingConfirmationAsync(responseDTO);

            logger.info("Booking completed successfully. Booking ID: {}", booking.getId());

            return responseDTO;
        } catch (FeignException e) {
            logger.error("Failed to get movie or showtime information", e);

            // Vẫn tạo response nhưng với thông tin tối thiểu
            BookingResponseDTO responseDTO = new BookingResponseDTO();
            responseDTO.setId(booking.getId());
            responseDTO.setShowtimeId(booking.getShowtimeId());
            responseDTO.setSeatNumber(booking.getSeatNumber());
            responseDTO.setCustomerName(booking.getCustomerName());
            responseDTO.setEmail(booking.getEmail());
            responseDTO.setPaid(booking.isPaid());

            return responseDTO;
        }
    }

    /**
     * Lấy thông tin chi tiết của một đơn đặt vé
     */
    public BookingResponseDTO getBookingById(Long bookingId) {
        logger.info("Getting booking details for ID: {}", bookingId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingException("Booking not found"));

        try {
            // Lấy thông tin suất chiếu
            ShowtimeDTO showtime = showtimeClient.getShowtimeById(booking.getShowtimeId());

            // Lấy thông tin phim
            MovieDTO movie = movieClient.getMovieById(showtime.getMovieId());

            // Chuyển đổi thành DTO để trả về
            BookingResponseDTO responseDTO = new BookingResponseDTO();
            responseDTO.setId(booking.getId());
            responseDTO.setShowtimeId(booking.getShowtimeId());
            responseDTO.setSeatNumber(booking.getSeatNumber());
            responseDTO.setCustomerName(booking.getCustomerName());
            responseDTO.setEmail(booking.getEmail());
            responseDTO.setPaid(booking.isPaid());
            responseDTO.setMovieTitle(movie.getTitle());
            responseDTO.setTheater(showtime.getTheater());
            responseDTO.setShowtimeDate(showtime.getTime());

            // Nếu chưa thanh toán, cung cấp URL thanh toán
            if (!booking.isPaid()) {
                responseDTO.setPaymentUrl("/api/payment/" + booking.getId());
            }

            return responseDTO;
        } catch (FeignException e) {
            logger.error("Failed to get movie or showtime information", e);

            // Vẫn tạo response nhưng với thông tin tối thiểu
            BookingResponseDTO responseDTO = new BookingResponseDTO();
            responseDTO.setId(booking.getId());
            responseDTO.setShowtimeId(booking.getShowtimeId());
            responseDTO.setSeatNumber(booking.getSeatNumber());
            responseDTO.setCustomerName(booking.getCustomerName());
            responseDTO.setEmail(booking.getEmail());
            responseDTO.setPaid(booking.isPaid());

            // Nếu chưa thanh toán, cung cấp URL thanh toán
            if (!booking.isPaid()) {
                responseDTO.setPaymentUrl("/api/payment/" + booking.getId());
            }

            return responseDTO;
        }
    }

    /**
     * Lấy danh sách các đơn đặt vé theo email
     */
    public List<BookingResponseDTO> getBookingsByEmail(String email) {
        logger.info("Getting bookings for email: {}", email);
        List<Booking> bookings = bookingRepository.findByEmail(email);

        return bookings.stream().map(booking -> {
            BookingResponseDTO responseDTO = new BookingResponseDTO();
            responseDTO.setId(booking.getId());
            responseDTO.setShowtimeId(booking.getShowtimeId());
            responseDTO.setSeatNumber(booking.getSeatNumber());
            responseDTO.setCustomerName(booking.getCustomerName());
            responseDTO.setEmail(booking.getEmail());
            responseDTO.setPaid(booking.isPaid());

            try {
                // Lấy thông tin suất chiếu
                ShowtimeDTO showtime = showtimeClient.getShowtimeById(booking.getShowtimeId());
                responseDTO.setTheater(showtime.getTheater());
                responseDTO.setShowtimeDate(showtime.getTime());

                // Lấy thông tin phim
                MovieDTO movie = movieClient.getMovieById(showtime.getMovieId());
                responseDTO.setMovieTitle(movie.getTitle());
            } catch (FeignException e) {
                logger.error("Failed to get movie or showtime information for booking ID: {}", booking.getId(), e);
                // Đặt giá trị mặc định nếu không lấy được thông tin
                responseDTO.setMovieTitle("Unknown Movie");
                responseDTO.setTheater("Unknown Theater");
                responseDTO.setShowtimeDate(LocalDateTime.now());
            }

            // Nếu chưa thanh toán, cung cấp URL thanh toán
            if (!booking.isPaid()) {
                responseDTO.setPaymentUrl("/api/payment/" + booking.getId());
            }

            return responseDTO;
        }).collect(Collectors.toList());
    }

    /**
     * Hủy đơn đặt vé và giải phóng ghế
     * Chỉ cho phép hủy đơn đặt vé chưa thanh toán
     */
    @Transactional
    public void cancelBooking(Long bookingId) {
        logger.info("Cancelling booking ID: {}", bookingId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingException("Booking not found"));

        // Chỉ cho phép hủy đơn chưa thanh toán
        if (booking.isPaid()) {
            throw new BookingException("Cannot cancel a paid booking");
        }

        try {
            // Giải phóng ghế trong SeatService
            SeatDTO seatDTO = new SeatDTO();
            seatDTO.setShowtimeId(booking.getShowtimeId());
            seatDTO.setSeatNumber(booking.getSeatNumber());
            seatDTO.setReserved(false);

            seatClient.releaseSeat(seatDTO);

            // Xóa booking khỏi database
            bookingRepository.delete(booking);

            // Giải phóng khóa Redis
            String lockKey = "seat:" + booking.getShowtimeId() + ":" + booking.getSeatNumber();
            redisTemplate.delete(lockKey);

            logger.info("Booking cancelled successfully. Booking ID: {}", bookingId);
        } catch (FeignException e) {
            logger.error("Failed to release seat for booking ID: {}", bookingId, e);
            throw new ServiceCommunicationException("Failed to release seat", e);
        }
    }

    /**
     * Scheduled task để giải phóng các ghế của đơn đặt vé quá hạn thanh toán
     */
    @Scheduled(fixedRate = 300000) // Chạy mỗi 5 phút
    @Transactional
    public void releaseExpiredBookings() {
        logger.info("Running scheduled task to release expired bookings");

        List<Booking> unpaidBookings = bookingRepository.findUnpaidBookings();

        for (Booking booking : unpaidBookings) {
            try {
                // Kiểm tra thời gian tạo booking, nếu quá 10 phút và chưa thanh toán
                ShowtimeDTO showtime = showtimeClient.getShowtimeById(booking.getShowtimeId());

                // Trong thực tế, nên lưu thời gian tạo booking trong Booking entity
                // Ở đây, giả sử rằng chúng ta kiểm tra dựa trên thời gian suất chiếu
                if (showtime.getTime().minusMinutes(30).isBefore(LocalDateTime.now())) {
                    try {
                        // Hủy booking và giải phóng ghế
                        cancelBooking(booking.getId());
                    } catch (Exception e) {
                        logger.error("Error cancelling expired booking ID: {}", booking.getId(), e);
                    }
                }
            } catch (FeignException e) {
                logger.error("Failed to get showtime for booking ID: {}", booking.getId(), e);
            }
        }
    }
}
