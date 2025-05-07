package com.example.movie_service.Repository;

import com.example.movie_service.Model.Showtime;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ShowtimeRepository extends JpaRepository<Showtime, Long> {
    List<Showtime> findByMovieId(Long movieId);
    List<Showtime> findByTimeBetween(LocalDateTime start, LocalDateTime end);
}
