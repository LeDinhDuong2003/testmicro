package com.example.booking_service.Client;

import com.example.booking_service.DTO.ShowtimeDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "movie-service") // ShowtimeService là một phần của MovieService
public interface ShowtimeClient {

    @GetMapping("/api/showtimes/{id}")
    ShowtimeDTO getShowtimeById(@PathVariable("id") Long id);
}