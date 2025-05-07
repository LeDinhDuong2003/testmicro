package com.example.seat_service.Model;

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

    @ManyToOne
    @JoinColumn(name = "showtime_id")
    private Showtime showtime;
}

