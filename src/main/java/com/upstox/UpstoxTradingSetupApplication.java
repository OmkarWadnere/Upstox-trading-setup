package com.upstox;

import com.mashape.unirest.http.exceptions.UnirestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@SpringBootApplication
public class UpstoxTradingSetupApplication {


	public static void main(String[] args) throws IOException, UnirestException, InterruptedException {
		SpringApplication.run(UpstoxTradingSetupApplication.class, args);
		System.out.println("Hello");
//		Unirest.setTimeouts(0, 0);

	}

}
