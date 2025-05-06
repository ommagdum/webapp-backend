package com.mlspamdetection.webapp_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WebappBackendApplication {
	/**
	 * The entry point of the Spring Boot application.
	 *
	 * This class contains the main method which is used to launch the application.
	 * It initializes the Spring application context and starts the embedded web server.
	 *
	 * Usage:
	 * To run the application, execute the main method.
	 */

	public static void main(String[] args) {
		SpringApplication.run(WebappBackendApplication.class, args);
	}

}
