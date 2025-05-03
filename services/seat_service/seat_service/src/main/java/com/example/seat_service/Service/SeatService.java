package com.example.seat_service.Service;

import com.example.seat_service.Model.Seat;
import com.example.seat_service.Repository.SeatRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
public class SeatService {

    private final SeatRepository seatRepo;
    private final RedisTemplate<String, Object> redisTemplate;

    public SeatService(SeatRepository seatRepo, RedisTemplate<String, Object> redisTemplate) {
        this.seatRepo = seatRepo;
        this.redisTemplate = redisTemplate;
    }

    public boolean tryReserveSeat(Long showtimeId, String seatNumber) {
        String lockKey = "seat:" + showtimeId + ":" + seatNumber;
        Boolean success = redisTemplate.opsForValue().setIfAbsent(lockKey, "locked", Duration.ofSeconds(100));

        if (Boolean.TRUE.equals(success)) {
            Optional<Seat> seatOptional = seatRepo.findByShowtimeIdAndSeatNumber(showtimeId, seatNumber);
            if (seatOptional.isPresent()) {
                Seat seat = seatOptional.get();
                if (!seat.isReserved()) {
                    seat.setReserved(true);
                    seatRepo.save(seat);
                    return true;
                }
            }
        }
        return false;
    }
}
