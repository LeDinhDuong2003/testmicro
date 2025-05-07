package com.example.booking_service.Controller;

import com.example.booking_service.DTO.BookingResponseDTO;
import com.example.booking_service.Exception.BookingException;
import com.example.booking_service.Exception.ServiceCommunicationException;
import com.example.booking_service.Service.BookingService;
import com.example.booking_service.Service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private BookingService bookingService;

    /**
     * API xử lý thanh toán cho một đơn đặt vé
     */
    @PostMapping("/{bookingId}")
    public ResponseEntity<?> processPayment(@PathVariable Long bookingId) {
        try {
            Map<String, Object> paymentResult = paymentService.processPayment(bookingId);

            if ((Boolean) paymentResult.get("success")) {
                // Nếu thanh toán thành công, hoàn tất đặt vé
                BookingResponseDTO bookingResponse = bookingService.completeBooking(bookingId);

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Payment processed successfully");
                response.put("booking", bookingResponse);

                return ResponseEntity.ok(response);
            } else {
                // Nếu thanh toán thất bại
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(paymentResult);
            }
        } catch (BookingException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Payment Failed");
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
     * API lấy thông tin thanh toán của một đơn đặt vé
     */
    @GetMapping("/{bookingId}")
    public ResponseEntity<?> getPaymentPage(@PathVariable Long bookingId) {
        try {
            BookingResponseDTO booking = bookingService.getBookingById(bookingId);

            if (booking.isPaid()) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "Booking is already paid");

                return ResponseEntity.ok(response);
            }

            // Trong thực tế, đây sẽ trả về trang thanh toán hoặc thông tin để redirect đến cổng thanh toán
            Map<String, Object> response = new HashMap<>();
            response.put("bookingId", bookingId);
            response.put("amount", 100000); // Giá vé mẫu
            response.put("movieTitle", booking.getMovieTitle());
            response.put("showtime", booking.getShowtimeDate().toString());
            response.put("paymentUrl", "/api/payment/" + bookingId);

            return ResponseEntity.ok(response);
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
}