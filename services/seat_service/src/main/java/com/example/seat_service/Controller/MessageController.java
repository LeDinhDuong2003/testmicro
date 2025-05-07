package com.example.seat_service.Controller;

import com.example.seat_service.Service.MessageProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MessageController {

    @Autowired
    private MessageProducer messageProducer;

    @GetMapping("/send-message")
    public String sendMessage() {
        messageProducer.sendMessage();
        return "Message sent!";
    }
}
