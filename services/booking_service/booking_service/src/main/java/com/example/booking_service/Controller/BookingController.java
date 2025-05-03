package com.example.booking_service.Controller;

import com.example.booking_service.DTO.BookingRequestDTO;
import com.example.booking_service.DTO.BookingResponseDTO;
import com.example.booking_service.Exception.BookingException;
import com.example.booking_service.Exception.ServiceCommunicationException;
import com.example.booking_service.Service.BookingService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    /**
     * API tạo đơn đặt vé mới
     * Sử dụng Circuit Breaker và Rate Limiter để bảo vệ API
     */
    @PostMapping
    @CircuitBreaker(name = "bookingService", fallbackMethod = "createBookingFallback")
    @RateLimiter(name = "bookingAPI")
    public ResponseEntity<?> createBooking(@RequestBody BookingRequestDTO requestDTO) {
        try {
            BookingResponseDTO responseDTO = bookingService.createBooking(requestDTO);
            return ResponseEntity.ok(responseDTO);
        } catch (BookingException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Booking Failed");
            errorResponse.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (ServiceCommunicationException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Service Communication Error");
            errorResponse.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
        }
    }

    /**
     * Fallback method cho Circuit Breaker
     */
    public ResponseEntity<?> createBookingFallback(BookingRequestDTO requestDTO, Exception e) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "Service Unavailable");
        errorResponse.put("message", "The booking service is currently unavailable. Please try again later.");

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
    }

    /**
     * API lấy thông tin chi tiết của một đơn đặt vé
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getBooking(@PathVariable Long id) {
        try {
            BookingResponseDTO responseDTO = bookingService.getBookingById(id);
            return ResponseEntity.ok(responseDTO);
        } catch (BookingException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Booking Not Found");
            errorResponse.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (ServiceCommunicationException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Service Communication Error");
            errorResponse.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
        }
    }

    /**
     * API lấy danh sách các đơn đặt vé theo email
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<?> getBookingsByEmail(@PathVariable String email) {
        try {
            List<BookingResponseDTO> bookings = bookingService.getBookingsByEmail(email);
            return ResponseEntity.ok(bookings);
        } catch (ServiceCommunicationException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Service Communication Error");
            errorResponse.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
        }
    }

    /**
     * API hủy đơn đặt vé
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelBooking(@PathVariable Long id) {
        try {
            bookingService.cancelBooking(id);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Booking cancelled successfully");

            return ResponseEntity.ok(response);
        } catch (BookingException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Cancellation Failed");
            errorResponse.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (ServiceCommunicationException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Service Communication Error");
            errorResponse.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
        }
    }
}