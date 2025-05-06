package com.example.seat_service.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


public class SeatDTO {
    private Long id;
    private String seatNumber;
    private boolean reserved;
    private Long showtimeId;

    public SeatDTO(Long id, String seatNumber, boolean reserved, Long showtimeId) {
        this.id = id;
        this.seatNumber = seatNumber;
        this.reserved = reserved;
        this.showtimeId = showtimeId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public boolean isReserved() {
        return reserved;
    }

    public void setReserved(boolean reserved) {
        this.reserved = reserved;
    }

    public Long getShowtimeId() {
        return showtimeId;
    }

    public void setShowtimeId(Long showtimeId) {
        this.showtimeId = showtimeId;
    }
}

