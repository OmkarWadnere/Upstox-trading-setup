package com.upstox.production.omkarLogin.service;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.upstox.production.centralconfiguration.dto.UpstoxLoginDto;
import com.upstox.production.centralconfiguration.entity.UpstoxLogin;
import com.upstox.production.centralconfiguration.excpetion.UpstoxException;
import com.upstox.production.centralconfiguration.repository.UpstoxLoginRepository;
import com.upstox.production.centralconfiguration.utility.CentralUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;

import static com.upstox.production.centralconfiguration.utility.CentralUtility.authenticatedUser;
import static com.upstox.production.centralconfiguration.utility.CentralUtility.fetchDataUserClientId;
import static com.upstox.production.centralconfiguration.utility.CentralUtility.fetchDataUserClientSecret;

@Service
@PropertySource("classpath:data.properties")
public class OmkarRedirectedService {

    @Autowired
    private Environment environment;

    @Autowired
    private UpstoxLoginRepository upstoxLoginRepository;


    public UpstoxLogin redirctedUrl(String code) throws IOException, UnirestException, UpstoxException {
        if (authenticatedUser) {
            HttpResponse<JsonNode> response = Unirest.post("https://api.upstox.com/v2/login/authorization/token")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Accept", "application/json")
                    .field("code", code)
                    .field("client_id", fetchDataUserClientId)
                    .field("client_secret", fetchDataUserClientSecret)
                    .field("redirect_uri", environment.getProperty("omkar_redirect_url"))
                    .field("grant_type", "authorization_code").asJson();
            // Print the response
            UpstoxLoginDto upstoxLoginDto = buildUpstoxLoginDtoFromHttpResponse(response.getBody());
            Optional<UpstoxLogin> optionalUpstoxLogin = upstoxLoginRepository.findByEmail(upstoxLoginDto.getEmail());
            UpstoxLogin upstoxLogin = null;
            if (optionalUpstoxLogin.isPresent()) {
                upstoxLogin = optionalUpstoxLogin.get();
                upstoxLogin.setAccess_token(upstoxLoginDto.getAccess_token());
            } else {
                upstoxLogin = buildUpstoxLogin(upstoxLoginDto);
            }
            CentralUtility.omkarSchedulerToken = "Bearer " + upstoxLogin.getAccess_token();
            return upstoxLoginRepository.save(upstoxLogin);
        } else {
            throw new UpstoxException("User is not authorized to access!!!");
        }
    }

    public UpstoxLoginDto buildUpstoxLoginDtoFromHttpResponse(JsonNode response) {
        return UpstoxLoginDto.builder()
                .email(response.getObject().get("email").toString())
                .access_token(response.getObject().get("access_token").toString())
                .is_active(Boolean.parseBoolean(response.getObject().get("is_active").toString()))
                .user_id(response.getObject().get("user_id").toString())
                .user_type(response.getObject().get("user_type").toString())
                .user_name(response.getObject().get("user_name").toString()).build();
    }

    public UpstoxLogin buildUpstoxLogin(UpstoxLoginDto upstoxLoginDto) {
        return UpstoxLogin.builder().access_token(upstoxLoginDto.getAccess_token())
                .email(upstoxLoginDto.getEmail())
                .user_id(upstoxLoginDto.getUser_id())
                .user_name(upstoxLoginDto.getUser_name())
                .is_active(upstoxLoginDto.getIs_active())
                .user_type(upstoxLoginDto.getUser_type()).build();
    }
}
