package com.example.booking_service.RabbitMQ;

import com.example.booking_service.Service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Consumer RabbitMQ để xử lý các yêu cầu gửi email bất đồng bộ
 */
@Component
public class EmailConsumer {

    private static final Logger logger = LoggerFactory.getLogger(EmailConsumer.class);

    @Autowired
    private EmailService emailService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private static final int MAX_RETRY_COUNT = 3;
    private static final String RETRY_COUNT_HEADER = "x-retry-count";

    /**
     * Xử lý các yêu cầu gửi email xác nhận đặt vé
     */
    @RabbitListener(queues = "booking.confirmation.emails")
    public void processBookingConfirmationEmail(Map<String, Object> emailData) {
        try {
            String email = (String) emailData.get("email");
            String bookingId = emailData.get("bookingId").toString();

            logger.info("Processing confirmation email for booking: {}", bookingId);

            // Tạo nội dung email
            String subject = "Đặt vé xem phim thành công";
            String content = emailService.createConfirmationEmailContent(emailData);

            // Gửi email
            emailService.sendEmail(email, subject, content);

            logger.info("Email sent successfully for booking: {}", bookingId);
        } catch (Exception e) {
            // Lấy số lần retry hiện tại
            Integer retryCount = (Integer) emailData.getOrDefault(RETRY_COUNT_HEADER, 0);

            if (retryCount < MAX_RETRY_COUNT) {
                // Tăng số lần retry
                emailData.put(RETRY_COUNT_HEADER, retryCount + 1);

                // Đợi một thời gian trước khi gửi lại (exponential backoff)
                try {
                    Thread.sleep((long) Math.pow(2, retryCount) * 1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }

                // Gửi lại message vào queue
                rabbitTemplate.convertAndSend(
                        "booking.email.exchange",
                        "email.confirmation",
                        emailData
                );
                logger.warn("Retrying email send (attempt {}): {}", retryCount + 1, e.getMessage());
            } else {
                // Đã vượt quá số lần retry tối đa
                logger.error("Failed to process email after {} attempts: {}", MAX_RETRY_COUNT, e.getMessage());
                // Có thể lưu vào DB hoặc log để xử lý thủ công sau
            }
        }
    }
}