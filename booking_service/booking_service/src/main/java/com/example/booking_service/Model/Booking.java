package com.example.booking_service.Model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "booking")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Booking {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String seatNumber;

    private String customerName;

    private String email;

    private boolean paid;

    // Lưu showtimeId thay vì dùng relationship
    private Long showtimeId;

    // Phương thức tiện ích để làm việc với ShowtimeDTO
    public Long getShowtimeId() {
        return showtimeId;
    }

    public void setShowtimeId(Long showtimeId) {
        this.showtimeId = showtimeId;
    }
}