package com.example.movie_service.Controller;

import com.example.movie_service.DTO.ShowtimeDTO;
import com.example.movie_service.Model.Showtime;
import com.example.movie_service.Service.ShowtimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/showtimes")
public class ShowtimeController {

    @Autowired
    private ShowtimeService showtimeService;

    @GetMapping
    public List<Showtime> getAllShowtimes() {
        return showtimeService.getAllShowtimes();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShowtimeDTO> getShowtimeById(@PathVariable Long id) {
        try {
            ShowtimeDTO showtimeDTO = showtimeService.getShowtimeById(id);
            return ResponseEntity.ok(showtimeDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/movie/{movieId}")
    public List<Showtime> getShowtimesByMovieId(@PathVariable Long movieId) {
        return showtimeService.getShowtimesByMovieId(movieId);
    }

    @GetMapping("/time-range")
    public List<Showtime> getShowtimesByTimeRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return showtimeService.getShowtimesByTimeRange(start, end);
    }

    @PostMapping
    public Showtime createShowtime(@RequestBody Showtime showtime) {
        return showtimeService.createShowtime(showtime);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Showtime> updateShowtime(@PathVariable Long id, @RequestBody Showtime showtimeDetails) {
        try {
            Showtime updatedShowtime = showtimeService.updateShowtime(id, showtimeDetails);
            return ResponseEntity.ok(updatedShowtime);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteShowtime(@PathVariable Long id) {
        try {
            showtimeService.deleteShowtime(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
