package com.zendesk.zcc;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableEncryptableProperties
public class ZccApplication {

	public static void main(String[] args) {
		SpringApplication.run(ZccApplication.class, args);
	}

}
