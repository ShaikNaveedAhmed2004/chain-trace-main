package com.supplychain;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class SupplyChainApplication {
    public static void main(String[] args) {
        SpringApplication.run(SupplyChainApplication.class, args);
    }
}
