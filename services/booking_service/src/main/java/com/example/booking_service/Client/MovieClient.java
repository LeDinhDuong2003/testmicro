package com.example.booking_service.Client;

import com.example.booking_service.DTO.MovieDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "movie-service" , url = "http://movie-service:8081")
public interface MovieClient {

    @GetMapping("/api/movies/{id}")
    MovieDTO getMovieById(@PathVariable("id") Long id);
}

