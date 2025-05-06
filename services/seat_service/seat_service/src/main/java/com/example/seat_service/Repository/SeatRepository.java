package com.example.seat_service.Repository;

import com.example.seat_service.Model.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    Optional<Seat> findByShowtimeIdAndSeatNumber(Long showtimeId, String seatNumber);
    List<Seat> findByShowtimeIdAndReservedFalse(Long showtimeId);
}
