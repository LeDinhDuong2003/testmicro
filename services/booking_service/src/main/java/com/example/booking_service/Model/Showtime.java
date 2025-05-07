package com.example.booking_service.Model;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "showtime")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Showtime {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "time", columnDefinition = "datetime")
    private LocalDateTime time;

    private String theater;

    @ManyToOne
    @JoinColumn(name = "movie_id")
    private Movie movie;

    @OneToMany(mappedBy = "showtime", cascade = CascadeType.ALL)
    private List<Seat> seats;

//    @OneToMany(mappedBy = "showtime", cascade = CascadeType.ALL)
//    private List<Booking> bookings;
}
