package com.example.movie_service.DTO;

import java.time.LocalDateTime;

public class ShowtimeDTO {
    private Long id;
    private LocalDateTime time;
    private String theater;
    private Long movieId;
    private String movieTitle; // Có thể được điền bởi MovieService

    public ShowtimeDTO(Long id, LocalDateTime time, String theater, Long movieId, String movieTitle) {
        this.id = id;
        this.time = time;
        this.theater = theater;
        this.movieId = movieId;
        this.movieTitle = movieTitle;
    }

    public ShowtimeDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

    public String getTheater() {
        return theater;
    }

    public void setTheater(String theater) {
        this.theater = theater;
    }

    public Long getMovieId() {
        return movieId;
    }

    public void setMovieId(Long movieId) {
        this.movieId = movieId;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public void setMovieTitle(String movieTitle) {
        this.movieTitle = movieTitle;
    }
}
