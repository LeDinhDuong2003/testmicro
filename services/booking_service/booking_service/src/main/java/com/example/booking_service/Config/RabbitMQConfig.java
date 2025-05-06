package com.example.booking_service.Config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Giữ lại queue ban đầu
    @Bean
    public Queue bookingQueue() {
        return new Queue("booking.queue", true); // durable = true
    }

    // Thêm các cấu hình cho email
    public static final String EMAIL_QUEUE = "booking.confirmation.emails";
    public static final String EMAIL_EXCHANGE = "booking.email.exchange";
    public static final String EMAIL_ROUTING_KEY = "email.confirmation";

    @Bean
    public Queue emailQueue() {
        return QueueBuilder.durable(EMAIL_QUEUE)
                .withArgument("x-dead-letter-exchange", "booking.email.dlx")
                .withArgument("x-dead-letter-routing-key", "email.dead")
                .build();
    }

    @Bean
    public DirectExchange emailExchange() {
        return new DirectExchange(EMAIL_EXCHANGE);
    }

    @Bean
    public Binding emailBinding() {
        return BindingBuilder
                .bind(emailQueue())
                .to(emailExchange())
                .with(EMAIL_ROUTING_KEY);
    }

    // Cấu hình Dead Letter Exchange để xử lý retry
    @Bean
    public Queue deadLetterQueue() {
        return new Queue("booking.email.dlq");
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange("booking.email.dlx");
    }

    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder
                .bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with("email.dead");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (!ack) {
                System.out.println("Failed to send message: " + cause);
            }
        });
        return template;
    }
}