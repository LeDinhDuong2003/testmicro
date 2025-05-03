package com.example.seat_service.Controller;

import com.example.seat_service.Model.Seat;
import com.example.seat_service.Service.SeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/seats")
public class SeatController {
    @Autowired
    private SeatService seatService;

    @PostMapping("/reserve")
    public ResponseEntity<String> reserveSeat(@RequestBody Seat seatRequest) {
        boolean reserved = seatService.tryReserveSeat(seatRequest.getShowtime().getId(), seatRequest.getSeatNumber());
        if (reserved) return ResponseEntity.ok("Seat reserved");
        return ResponseEntity.status(HttpStatus.CONFLICT).body("Seat already reserved");
    }
}