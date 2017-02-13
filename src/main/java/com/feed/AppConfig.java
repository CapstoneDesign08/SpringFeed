package com.feed;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;

import javax.servlet.SessionTrackingMode;
import java.util.Collections;

@SpringBootApplication
public class AppConfig {
    public static void main(String[] args) {
        SpringApplication.run(AppConfig.class, args);
    }

    @Bean
    ServletContextInitializer servletContextInitializer() {
        // keep session only in cookie so that jsessionid is not appended to URL.
        return servletContext -> servletContext.setSessionTrackingModes(
                Collections.singleton(SessionTrackingMode.COOKIE));
    }
}
