package com.upstox.production.centralconfiguration.service;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.upstox.production.centralconfiguration.excpetion.UpstoxException;
import com.upstox.production.centralconfiguration.repository.TradeAccessUpstoxLoginRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import static com.upstox.production.centralconfiguration.utility.CentralUtility.authenticatedUser;
import static com.upstox.production.centralconfiguration.utility.CentralUtility.fetchDataUserClientId;

@Service
@PropertySource("classpath:data.properties")
public class FetchDataAccessUpstoxLoginService {

    @Autowired
    private TradeAccessUpstoxLoginRepository tradeAccessUpstoxLoginRepository;
    @Autowired
    private Environment environment;


    public String getUpstoxLoginUrl() throws UnirestException, UpstoxException {
        if (authenticatedUser) {
            String authorizationURL = "https://api.upstox.com/v2/login/authorization/dialog?client_id=" + fetchDataUserClientId + "&redirect_uri=" + environment.getProperty("omkar_redirect_url");
            HttpResponse response = Unirest.get(authorizationURL)
                    .asString();
            return "https://api.upstox.com/v2/login/authorization/dialog?client_id=" + fetchDataUserClientId + "&redirect_uri=" + environment.getProperty("omkar_redirect_url");
        } else {
            throw new UpstoxException("User is not authorized to access!!!");
        }
    }
}
