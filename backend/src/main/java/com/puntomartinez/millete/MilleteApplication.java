package com.puntomartinez.millete;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.context.annotation.ImportRuntimeHints;
import com.puntomartinez.millete.shared.infrastructure.config.NativeRuntimeHints;

@ImportRuntimeHints(NativeRuntimeHints.class)
@EnableScheduling
@SpringBootApplication
public class MilleteApplication {

	public static void main(String[] args) {
		SpringApplication.run(MilleteApplication.class, args);
	}

}
