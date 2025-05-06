package com.example.seat_service.Controller;

import com.example.seat_service.DTO.SeatDTO;
import com.example.seat_service.Model.Seat;
import com.example.seat_service.Service.SeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/seats")
public class SeatController {
    @Autowired
    private SeatService seatService;

    @PostMapping("/reserve/{showtimeId}/{seatNumber}")
    public ResponseEntity<String> reserveSeat(
            @PathVariable Long showtimeId,
            @PathVariable String seatNumber) {
        boolean reserved = seatService.tryReserveSeat(showtimeId, seatNumber);
        if (reserved) {
            return ResponseEntity.ok("Seat reserved successfully");
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Seat is already reserved or unavailable");
        }
    }


    @GetMapping("/{showtimeId}/{seatNumber}")
    public ResponseEntity<SeatDTO> getSeatByShowtimeAndNumber(
            @PathVariable Long showtimeId,
            @PathVariable String seatNumber) {
        SeatDTO seatDTO = seatService.getSeatByShowtimeAndNumber(showtimeId, seatNumber);
        return ResponseEntity.ok(seatDTO);
    }

    @GetMapping("/{showtimeId}/available")
    public ResponseEntity<List<SeatDTO>> getAvailableSeats(@PathVariable Long showtimeId) {
        List<SeatDTO> availableSeats = seatService.getAvailableSeats(showtimeId);
        return ResponseEntity.ok(availableSeats);
    }
    @PostMapping("/release")
    public ResponseEntity<String> releaseSeat(@RequestBody SeatDTO seatDTO) {
        boolean released = seatService.releaseSeat(seatDTO);
        if (released) {
            return ResponseEntity.ok("Seat released successfully");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Failed to release seat");
        }
    }


}