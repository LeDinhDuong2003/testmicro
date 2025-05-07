package com.example.booking_service.DTO;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class MovieDTO {
    private Long id;
    private String title;
    private String description;

    // Getters và setters đã được tạo tự động bởi annotation @Data của Lombok
    // Lombok sẽ tự động tạo các phương thức sau:
    // getId(), getTitle(), getDescription()
    // setId(), setTitle(), setDescription()
}