package com.subh.DemoChat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class DemoChatApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoChatApplication.class, args);
	}

}
