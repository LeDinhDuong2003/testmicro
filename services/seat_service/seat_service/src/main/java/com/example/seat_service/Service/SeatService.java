package com.example.seat_service.Service;

import com.example.seat_service.DTO.SeatDTO;
import com.example.seat_service.Model.Seat;
import com.example.seat_service.Repository.SeatRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class SeatService {

    private final SeatRepository seatRepo;
    private final RedisTemplate<String, Object> redisTemplate;

    public SeatService(SeatRepository seatRepo, RedisTemplate<String, Object> redisTemplate) {
        this.seatRepo = seatRepo;
        this.redisTemplate = redisTemplate;
    }

    public boolean tryReserveSeat(Long showtimeId, String seatNumber) {
        String lockKey = "seat:" + showtimeId + ":" + seatNumber;
        Boolean success = redisTemplate.opsForValue().setIfAbsent(lockKey, "locked", Duration.ofSeconds(100));
//        if (Boolean.TRUE.equals(success)) {
//            Optional<Seat> seatOptional = seatRepo.findByShowtimeIdAndSeatNumber(showtimeId, seatNumber);
//            if (seatOptional.isPresent()) {
//                Seat seat = seatOptional.get();
//                if (!seat.isReserved()) {
//                    seat.setReserved(true);
//                    seatRepo.save(seat);
//                    return true;
//                }
//            }
//        }
        if (Boolean.TRUE.equals(success)) return true;
        return false;
    }
    public SeatDTO getSeatByShowtimeAndNumber(Long showtimeId, String seatNumber) {
        Seat seat = seatRepo.findByShowtimeIdAndSeatNumber(showtimeId, seatNumber)
                .orElseThrow(() -> new RuntimeException("Seat not found"));

        // Kiểm tra xem có key Redis đang lock ghế này không
        String lockKey = "seat:" + showtimeId + ":" + seatNumber;
        boolean isLocked = Boolean.TRUE.equals(redisTemplate.hasKey(lockKey));

        return new SeatDTO(
                seat.getId(),
                seat.getSeatNumber(),
                seat.isReserved() || isLocked, // nếu bị Redis lock thì xem như đã reserved
                seat.getShowtime().getId()
        );
    }


    public List<SeatDTO> getAvailableSeats(Long showtimeId) {
        // 1. Lấy tất cả ghế chưa bị reserved trong DB
        List<Seat> unreservedSeats = seatRepo.findByShowtimeIdAndReservedFalse(showtimeId);

        // 2. Kiểm tra xem có ghế nào đang bị Redis lock
        List<SeatDTO> availableSeats = new ArrayList<>();

        for (Seat seat : unreservedSeats) {
            String lockKey = "seat:" + showtimeId + ":" + seat.getSeatNumber();
            Boolean isLocked = redisTemplate.hasKey(lockKey);

            if (Boolean.FALSE.equals(isLocked)) {
                availableSeats.add(new SeatDTO(
                        seat.getId(),
                        seat.getSeatNumber(),
                        false,
                        seat.getShowtime().getId()
                ));
            }
        }

        return availableSeats;
    }

    public boolean releaseSeat(SeatDTO seatDTO) {
        Optional<Seat> seatOpt = seatRepo.findByShowtimeIdAndSeatNumber(
                seatDTO.getShowtimeId(), seatDTO.getSeatNumber());

        if (seatOpt.isPresent()) {
            Seat seat = seatOpt.get();
            seat.setReserved(false);
            seatRepo.save(seat);
            return true;
        }
        return false;
    }


}
