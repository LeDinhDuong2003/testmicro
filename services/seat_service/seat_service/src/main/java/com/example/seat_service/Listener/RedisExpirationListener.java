package com.example.seat_service.Listener;

import com.example.seat_service.Repository.SeatRepository;
import com.example.seat_service.Model.Seat;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Component
public class RedisExpirationListener implements MessageListener {

    @Autowired
    private SeatRepository seatRepo;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = message.toString(); // VD: seat:1:A1
        System.out.println("Key expired: " + expiredKey);

        if (expiredKey.startsWith("seat:")) {
            try {
                String[] parts = expiredKey.split(":");
                Long showtimeId = Long.parseLong(parts[1]);
                String seatNumber = parts[2];

                seatRepo.findByShowtimeIdAndSeatNumber(showtimeId, seatNumber)
                        .ifPresent(seat -> {
                            seat.setReserved(false);
                            seatRepo.save(seat);
                            System.out.println("Auto rollback seat (TTL expired): " + seatNumber);
                        });
            } catch (Exception e) {
                System.err.println("Error parsing expired key: " + expiredKey);
                e.printStackTrace();
            }
        }
    }
}
