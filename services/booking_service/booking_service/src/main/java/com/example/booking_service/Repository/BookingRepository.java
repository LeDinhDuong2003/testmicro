package com.example.booking_service.Repository;

import com.example.booking_service.Model.Booking;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByShowtimeId(Long showtimeId);

    List<Booking> findByEmail(String email);

    List<Booking> findByShowtimeIdAndSeatNumber(Long showtimeId, String seatNumber);

    @Query("SELECT b FROM Booking b WHERE b.paid = false")
    List<Booking> findUnpaidBookings();

    @Query("SELECT b FROM Booking b WHERE b.paid = false AND b.createdAt < :expirationTime")
    List<Booking> findExpiredUnpaidBookings(@Param("expirationTime") LocalDateTime expirationTime);
}