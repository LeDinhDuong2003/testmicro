package com.example.booking_service.Service;

import com.example.booking_service.Exception.BookingException;
import com.example.booking_service.Model.Booking;
import com.example.booking_service.Repository.BookingRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    @Autowired
    private BookingRepository bookingRepository;

    /**
     * Xử lý thanh toán cho một đơn đặt vé
     * Circuit Breaker để xử lý khi payment gateway không khả dụng
     */
    @CircuitBreaker(name = "paymentService", fallbackMethod = "processPaymentFallback")
    @Transactional
    public Map<String, Object> processPayment(Long bookingId) {
        logger.info("Processing payment for booking ID: {}", bookingId);
        Map<String, Object> result = new HashMap<>();

        // Tìm booking
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingException("Booking not found with id: " + bookingId));

        // Kiểm tra trạng thái thanh toán
        if (booking.isPaid()) {
            result.put("success", false);
            result.put("message", "Booking already paid");
            return result;
        }

        try {
            // Trong thực tế, đây là nơi tích hợp với payment gateway (VNPay, Stripe, PayPal, ...)
            // Đây là mô phỏng xử lý thanh toán thành công
            boolean paymentSuccess = simulatePaymentGateway();

            if (paymentSuccess) {
                // Cập nhật trạng thái thanh toán trong database
                booking.setPaid(true);
                bookingRepository.save(booking);

                result.put("success", true);
                result.put("message", "Payment successful");
                logger.info("Payment successful for booking ID: {}", bookingId);
            } else {
                result.put("success", false);
                result.put("message", "Payment failed");
                logger.info("Payment failed for booking ID: {}", bookingId);
            }

            return result;
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Error processing payment: " + e.getMessage());
            logger.error("Error processing payment for booking ID: {}", bookingId, e);
            return result;
        }
    }

    /**
     * Phương thức fallback khi payment gateway không khả dụng
     */
    public Map<String, Object> processPaymentFallback(Long bookingId, Exception e) {
        logger.error("Payment service is unavailable. Fallback triggered for booking ID: {}", bookingId, e);
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", "Payment service is currently unavailable. Please try again later.");
        return result;
    }

    /**
     * Mô phỏng gọi đến payment gateway
     * Trong thực tế, đây sẽ là tích hợp với cổng thanh toán thật
     */
    private boolean simulatePaymentGateway() {
        // Giả định thanh toán luôn thành công
        // Trong thực tế, đây sẽ là gọi API đến cổng thanh toán
        return true;
    }
}
