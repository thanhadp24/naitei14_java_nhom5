package vn.sun.public_service_manager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class PublicServiceManagerApplication {

	public static void main(String[] args) {
		SpringApplication.run(PublicServiceManagerApplication.class, args);
	}

}
