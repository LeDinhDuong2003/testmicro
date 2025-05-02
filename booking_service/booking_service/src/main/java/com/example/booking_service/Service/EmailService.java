package com.example.booking_service.Service;

import com.example.booking_service.DTO.BookingResponseDTO;
import com.example.booking_service.Model.Booking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.HashMap;
import java.util.Map;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private KafkaTemplate<String, Map<String, Object>> kafkaTemplate;

    private static final String EMAIL_TOPIC = "booking-confirmation-emails";

    /**
     * Gửi email xác nhận đặt vé thông qua Kafka
     */
    public void sendBookingConfirmationAsync(BookingResponseDTO bookingResponse) {
        try {
            Map<String, Object> emailData = new HashMap<>();
            emailData.put("bookingId", bookingResponse.getId());
            emailData.put("email", bookingResponse.getEmail());
            emailData.put("customerName", bookingResponse.getCustomerName());
            emailData.put("seatNumber", bookingResponse.getSeatNumber());
            emailData.put("movieTitle", bookingResponse.getMovieTitle());
            emailData.put("theater", bookingResponse.getTheater());
            emailData.put("showtime", bookingResponse.getShowtimeDate().toString());

            // Gửi thông tin email đến Kafka
            kafkaTemplate.send(EMAIL_TOPIC, emailData);
            logger.info("Email request sent to Kafka for booking ID: {}", bookingResponse.getId());
        } catch (Exception e) {
            logger.error("Failed to send email request to Kafka", e);
        }
    }

    /**
     * Gửi email trực tiếp (được gọi từ Kafka consumer)
     */
    public void sendEmail(String to, String subject, String content) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true); // true để sử dụng HTML

            mailSender.send(message);
            logger.info("Email sent successfully to: {}", to);
        } catch (MessagingException e) {
            logger.error("Failed to send email", e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    /**
     * Tạo nội dung email xác nhận đặt vé
     */
    public String createConfirmationEmailContent(Map<String, Object> emailData) {
        String customerName = (String) emailData.get("customerName");
        String movieTitle = (String) emailData.get("movieTitle");
        String theater = (String) emailData.get("theater");
        String showtime = (String) emailData.get("showtime");
        String seatNumber = (String) emailData.get("seatNumber");

        return "<html>" +
                "<head>" +
                "   <style>" +
                "       body { font-family: Arial, sans-serif; }" +
                "       .container { width: 600px; margin: 0 auto; }" +
                "       .header { background-color: #004d99; color: white; padding: 20px; text-align: center; }" +
                "       .content { padding: 20px; }" +
                "       .footer { background-color: #f2f2f2; padding: 10px; text-align: center; font-size: 12px; }" +
                "   </style>" +
                "</head>" +
                "<body>" +
                "   <div class='container'>" +
                "       <div class='header'>" +
                "           <h1>Xác nhận đặt vé xem phim</h1>" +
                "       </div>" +
                "       <div class='content'>" +
                "           <p>Xin chào " + customerName + ",</p>" +
                "           <p>Cảm ơn bạn đã đặt vé xem phim. Đơn đặt vé của bạn đã được xác nhận.</p>" +
                "           <p><strong>Chi tiết đặt vé:</strong></p>" +
                "           <ul>" +
                "               <li>Phim: " + movieTitle + "</li>" +
                "               <li>Rạp: " + theater + "</li>" +
                "               <li>Suất chiếu: " + showtime + "</li>" +
                "               <li>Ghế: " + seatNumber + "</li>" +
                "           </ul>" +
                "           <p>Vui lòng đến trước giờ chiếu 15 phút để lấy vé. Chúc bạn có buổi xem phim vui vẻ!</p>" +
                "       </div>" +
                "       <div class='footer'>" +
                "           <p>Đây là email tự động, vui lòng không trả lời email này.</p>" +
                "       </div>" +
                "   </div>" +
                "</body>" +
                "</html>";
    }
}
