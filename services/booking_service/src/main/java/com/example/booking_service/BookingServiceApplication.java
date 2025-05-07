package com.example.booking_service;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
//import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableDiscoveryClient  // Đăng ký với Service Discovery
@EnableFeignClients     // Kích hoạt Feign Client để gọi đến các service khác
//@EnableKafka            // Kích hoạt Kafka cho giao tiếp bất đồng bộ
@EnableScheduling
@EnableRabbit// Kích hoạt lập lịch cho các tác vụ định kỳ
public class BookingServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(BookingServiceApplication.class, args);
	}

}
