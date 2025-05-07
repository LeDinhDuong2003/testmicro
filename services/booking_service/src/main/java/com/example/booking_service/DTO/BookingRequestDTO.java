package com.example.booking_service.DTO;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequestDTO {
    private Long showtimeId;
    private String seatNumber;
    private String customerName;
    private String email;

    // Getters và setters đã được tạo tự động bởi annotation @Data của Lombok
    // Lombok sẽ tự động tạo các phương thức sau:
    // getShowtimeId(), getSeatNumber(), getCustomerName(), getEmail()
    // setShowtimeId(), setSeatNumber(), setCustomerName(), setEmail()
}