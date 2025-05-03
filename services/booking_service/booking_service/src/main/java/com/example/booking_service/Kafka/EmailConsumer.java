package com.example.booking_service.Kafka;

import com.example.booking_service.Service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Consumer Kafka để xử lý các yêu cầu gửi email bất đồng bộ
 */
@Component
public class EmailConsumer {

    private static final Logger logger = LoggerFactory.getLogger(EmailConsumer.class);

    @Autowired
    private EmailService emailService;

    /**
     * Xử lý các yêu cầu gửi email xác nhận đặt vé
     * Sử dụng ack thủ công để đảm bảo message được xử lý thành công
     */
    @KafkaListener(topics = "booking-confirmation-emails", containerFactory = "kafkaListenerContainerFactory")
    public void processBookingConfirmationEmail(Map<String, Object> emailData, Acknowledgment ack) {
        try {
            String email = (String) emailData.get("email");
            String bookingId = emailData.get("bookingId").toString();

            logger.info("Processing confirmation email for booking: {}", bookingId);

            // Tạo nội dung email
            String subject = "Đặt vé xem phim thành công";
            String content = emailService.createConfirmationEmailContent(emailData);

            // Gửi email
            emailService.sendEmail(email, subject, content);

            // Xác nhận message đã được xử lý
            ack.acknowledge();

            logger.info("Email sent successfully for booking: {}", bookingId);
        } catch (Exception e) {
            // Log lỗi - message sẽ được xử lý lại sau
            logger.error("Failed to process email: {}", e.getMessage());

            // Không ack message để nó được xử lý lại
        }
    }
}