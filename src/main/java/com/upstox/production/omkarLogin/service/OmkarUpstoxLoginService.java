package com.upstox.production.omkarLogin.service;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.upstox.production.centralconfiguration.repository.UpstoxLoginRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
@PropertySource("classpath:data.properties")
public class OmkarUpstoxLoginService {

    @Autowired
    private UpstoxLoginRepository upstoxLoginRepository;
    @Autowired
    private Environment environment;


    public String getUpstoxLoginUrl() throws UnirestException {
        String authorizationURL = "https://api.upstox.com/v2/login/authorization/dialog?client_id=" + environment.getProperty("omkar_client_id") + "&redirect_uri=" + environment.getProperty("omkar_redirect_url");
        HttpResponse response = Unirest.get(authorizationURL)
                .asString();
        return "https://api.upstox.com/v2/login/authorization/dialog?client_id=" + environment.getProperty("omkar_client_id") + "&redirect_uri=" + environment.getProperty("omkar_redirect_url");
    }
}
