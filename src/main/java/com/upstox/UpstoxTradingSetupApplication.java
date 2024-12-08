package com.upstox;

import com.upstox.production.centralconfiguration.security.SHA256Generator;
import com.upstox.production.centralconfiguration.utility.CentralUtility;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.security.KeyPair;

import static com.upstox.production.centralconfiguration.security.RSAGenerator.generateKeyPair;


@SpringBootApplication
@EnableAsync
@EnableScheduling
public class UpstoxTradingSetupApplication {

    @Autowired
    private SHA256Generator sha256Generator;

    @PostConstruct
    public void init() throws Exception {
        CentralUtility.nonMarketHourCode = sha256Generator.generateRandomSHA256();
        KeyPair keyPair = generateKeyPair();
        CentralUtility.privateKey = keyPair.getPrivate();
        CentralUtility.publicKey = keyPair.getPublic();

    }

    public static void main(String[] args) {
        SpringApplication.run(UpstoxTradingSetupApplication.class, args);
    }

}
