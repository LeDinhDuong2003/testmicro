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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Getters và setters đã được tạo tự động bởi annotation @Data của Lombok
}