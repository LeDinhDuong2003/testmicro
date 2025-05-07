package com.example.booking_service.DTO;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ShowtimeDTO {
    private Long id;
    private LocalDateTime time;
    private String theater;
    private Long movieId;
    private String movieTitle; // Có thể được điền bởi MovieService

    // Getters và setters đã được tạo tự động bởi annotation @Data của Lombok
    // Lombok sẽ tự động tạo các phương thức sau:
    // getId(), getTime(), getTheater(), getMovieId(), getMovieTitle()
    // setId(), setTime(), setTheater(), setMovieId(), setMovieTitle()
}