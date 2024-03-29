package com.upstox;

import com.mashape.unirest.http.exceptions.UnirestException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

import java.io.IOException;

@SpringBootApplication
@EnableAsync
public class UpstoxTradingSetupApplication {


	public static void main(String[] args) throws IOException, UnirestException, InterruptedException {
		SpringApplication.run(UpstoxTradingSetupApplication.class, args);
		System.out.println("Hello");

	}

}
