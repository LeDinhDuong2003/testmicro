package com.example.seat_service.Listener;

import com.example.seat_service.Repository.SeatRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class SeatRollbackListener {

    private final SeatRepository seatRepo;
    private final RedisTemplate<String, Object> redisTemplate;
    public SeatRollbackListener(SeatRepository seatRepo, RedisTemplate<String, Object> redisTemplate) {
        this.seatRepo = seatRepo;
        this.redisTemplate = redisTemplate;
    }

    @RabbitListener(queues = "booking.queue")
    public void handleBookingRollback(String message) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Map<String, Object> map = objectMapper.readValue(message, Map.class);

            Long showtimeId = Long.valueOf(map.get("showtimeId").toString());
            String seatNumber = map.get("seatNumber").toString();
            String status = map.get("status").toString();

            String lockKey = "seat:" + showtimeId + ":" + seatNumber;

            if ("FAILED".equalsIgnoreCase(status)) {
                // Xoá Redis key
                redisTemplate.delete(lockKey);
                System.out.println("Reserved seat " + seatNumber + " for showtime " + showtimeId+" failed");
            } else if ("SUCCESS".equalsIgnoreCase(status)) {
                // Xoá Redis key, luu db
                redisTemplate.delete(lockKey);
                seatRepo.findByShowtimeIdAndSeatNumber(showtimeId, seatNumber)
                        .ifPresent(seat -> {
                            seat.setReserved(true);
                            seatRepo.save(seat);
                            System.out.println("Reserved seat " + seatNumber + " for showtime " + showtimeId+" success");
                        });
                System.out.println("Reservation succeeded, Redis key removed for seat " + seatNumber);
            } else {
                System.out.println("Unknown status: " + status);
            }

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
