package com.example.seat_service.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class MessageProducer {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void sendMessage() {
        String message = "{\"showtimeId\": 1, \"seatNumber\": \"A1\", \"status\": \"SUCCESS\"}";
        rabbitTemplate.convertAndSend("booking.queue", message); // Gửi trực tiếp String
        System.out.println("Message Sent: " + message);
    }
}
