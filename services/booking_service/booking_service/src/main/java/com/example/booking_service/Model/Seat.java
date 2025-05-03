package com.example.booking_service.Model;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "seat")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Seat {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String seatNumber;

    private boolean reserved;
    @ManyToOne
    @JoinColumn(name = "showtime_id")
    private Showtime showtime;
}

