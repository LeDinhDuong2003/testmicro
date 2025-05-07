package com.example.booking_service.Config;

import feign.Logger;
import feign.Request;
import feign.Retryer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class FeignConfig {

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    @Bean
    public Request.Options options() {
        return new Request.Options(
                5, TimeUnit.SECONDS, // Connection Timeout
                10, TimeUnit.SECONDS, // Read Timeout
                true); // Follow Redirects
    }

    @Bean
    public Retryer retryer() {
        // Cấu hình retry: thử lại tối đa 3 lần, với khoảng thời gian tăng dần
        return new Retryer.Default(
                100, // Thời gian chờ ban đầu (ms)
                TimeUnit.SECONDS.toMillis(1), // Thời gian chờ tối đa (ms)
                3); // Số lần thử lại tối đa
    }
}