package com.example.booking_service.DTO;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SeatDTO {
    private Long id;
    private String seatNumber;
    private boolean reserved;
    private Long showtimeId;

    // Getters và setters đã được tạo tự động bởi annotation @Data của Lombok
    // Lombok sẽ tự động tạo các phương thức sau:
    // getId(), getSeatNumber(), isReserved(), getShowtimeId()
    // setId(), setSeatNumber(), setReserved(), setShowtimeId()
}