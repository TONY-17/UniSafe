package com.backend.escort;

import com.backend.escort.security.service.ImageStorageService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.Resource;


@SpringBootApplication

public class EscortApplication implements CommandLineRunner {
	@Resource
	ImageStorageService imageStorageService;
	public static void main(String[] args) {
		SpringApplication.run(EscortApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		imageStorageService.deleteAll();
		imageStorageService.init();
	}
}
