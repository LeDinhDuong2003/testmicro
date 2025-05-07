package com.example.movie_service.Service;

import com.example.movie_service.DTO.ShowtimeDTO;
import com.example.movie_service.Model.Showtime;
import com.example.movie_service.Repository.ShowtimeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ShowtimeService {

    @Autowired
    private ShowtimeRepository showtimeRepository;

    public List<Showtime> getAllShowtimes() {
        return showtimeRepository.findAll();
    }

    public ShowtimeDTO getShowtimeById(Long id) {
        Showtime showtime = showtimeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Showtime not found"));

        ShowtimeDTO dto = new ShowtimeDTO();
        dto.setId(showtime.getId());
        dto.setTime(showtime.getTime());
        dto.setTheater(showtime.getTheater());

        // Gán thông tin movie nếu có
        if (showtime.getMovie() != null) {
            dto.setMovieId(showtime.getMovie().getId());
            dto.setMovieTitle(showtime.getMovie().getTitle());
        }

        return dto;
    }

    public List<Showtime> getShowtimesByMovieId(Long movieId) {
        return showtimeRepository.findByMovieId(movieId);
    }

    public List<Showtime> getShowtimesByTimeRange(LocalDateTime start, LocalDateTime end) {
        return showtimeRepository.findByTimeBetween(start, end);
    }

    public Showtime createShowtime(Showtime showtime) {
        return showtimeRepository.save(showtime);
    }

    public Showtime updateShowtime(Long id, Showtime showtimeDetails) {
        Showtime showtime = showtimeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Showtime not found"));

        showtime.setTime(showtimeDetails.getTime());
        showtime.setTheater(showtimeDetails.getTheater());
        showtime.setMovie(showtimeDetails.getMovie());
        return showtimeRepository.save(showtime);
    }

    public void deleteShowtime(Long id) {
        showtimeRepository.deleteById(id);
    }
}