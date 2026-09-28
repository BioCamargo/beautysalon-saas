package com.beautysalon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.beautysalon")
@org.springframework.scheduling.annotation.EnableScheduling
@org.springframework.data.jpa.repository.config.EnableJpaAuditing
public class BeautySalonApplication {

    public static void main(String[] args) {
        SpringApplication.run(BeautySalonApplication.class, args);
    }
}
