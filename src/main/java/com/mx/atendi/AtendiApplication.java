package com.mx.atendi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class AtendiApplication {

	public static void main(String[] args) {
		SpringApplication.run(AtendiApplication.class, args);
	}

}
