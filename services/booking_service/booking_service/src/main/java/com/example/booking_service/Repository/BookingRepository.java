package com.example.booking_service.Repository;

import com.example.booking_service.Model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByShowtimeId(Long showtimeId);

    List<Booking> findByEmail(String email);

    List<Booking> findByShowtimeIdAndSeatNumber(Long showtimeId, String seatNumber);

    @Query("SELECT b FROM Booking b WHERE b.paid = false")
    List<Booking> findUnpaidBookings();
}