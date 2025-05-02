package com.example.booking_service.Client;

import com.example.booking_service.DTO.SeatDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "seat-service")
public interface SeatClient {

    @GetMapping("/api/seats/{showtimeId}/{seatNumber}")
    SeatDTO getSeatByShowtimeAndNumber(
            @PathVariable("showtimeId") Long showtimeId,
            @PathVariable("seatNumber") String seatNumber);

    @PostMapping("/api/seats/reserve")
    SeatDTO reserveSeat(@RequestBody SeatDTO seatDTO);

    @PostMapping("/api/seats/release")
    void releaseSeat(@RequestBody SeatDTO seatDTO);
}