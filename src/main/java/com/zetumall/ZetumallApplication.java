package com.zetumall;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class ZetumallApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZetumallApplication.class, args);
    }

}
