package com.example.booking_service.DTO;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class BookingResponseDTO {
    private Long id;
    private Long showtimeId;
    private String seatNumber;
    private String customerName;
    private String email;
    private boolean paid;
    private String movieTitle;
    private String theater;
    private LocalDateTime showtimeDate;
    private String paymentUrl;

    // Getters và setters đã được tạo tự động bởi annotation @Data của Lombok
    // Lombok sẽ tự động tạo các phương thức sau:
    // getId(), getShowtimeId(), getSeatNumber(), getCustomerName(), getEmail(), isPaid(), getMovieTitle(), getTheater(), getShowtimeDate(), getPaymentUrl()
    // setId(), setShowtimeId(), setSeatNumber(), setCustomerName(), setEmail(), setPaid(), setMovieTitle(), setTheater(), setShowtimeDate(), setPaymentUrl()
}