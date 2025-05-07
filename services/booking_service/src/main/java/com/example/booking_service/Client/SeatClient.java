package com.example.booking_service.Client;

import com.example.booking_service.DTO.SeatDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "seat-service", url = "http://seat-service:8082")
public interface SeatClient {

    @GetMapping("/api/seats/{showtimeId}/{seatNumber}")
    SeatDTO getSeatByShowtimeAndNumber(
            @PathVariable("showtimeId") Long showtimeId,
            @PathVariable("seatNumber") String seatNumber);

    @PostMapping("/api/seats/reserve/{showtimeId}/{seatNumber}")
    String reserveSeat(
            @PathVariable("showtimeId") Long showtimeId,
            @PathVariable("seatNumber") String seatNumber);
    @PostMapping("/api/seats/release")
    void releaseSeat(@RequestBody SeatDTO seatDTO);
}