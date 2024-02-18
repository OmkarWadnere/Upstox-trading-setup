package com.upstox.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.upstox.production.centralconfiguration.dto.GetPositionResponseDto;
import com.upstox.production.centralconfiguration.entity.UpstoxLogin;
import com.upstox.production.centralconfiguration.repository.UpstoxLoginRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/getHoldings")
public class GetHoldingTest {

    @Autowired
    private UpstoxLoginRepository upstoxLoginRepository;

    @Autowired
    private Environment environment;

    @GetMapping("/get")
    @ResponseStatus(HttpStatus.OK)
    public GetPositionResponseDto getHoldings() throws UnirestException, JsonProcessingException {
        Optional<UpstoxLogin> optionalUpstoxLogin = upstoxLoginRepository.findByEmail(environment.getProperty("email_id"));
        UpstoxLogin upstoxLogin = optionalUpstoxLogin.get();
        String token = "Bearer " + upstoxLogin.getAccess_token();
        Unirest.setTimeouts(0, 0);
        HttpResponse<String> response = Unirest.get("https://api.upstox.com/v2/portfolio/short-term-positions")
                .header("Accept", "application/json")
                .header("Authorization", token)
                .asString();

        System.out.println("Kya mila be : " + response);
        ObjectMapper objectMapper = new ObjectMapper();

        GetPositionResponseDto getPositionResponseDto = objectMapper.readValue(response.getBody(), GetPositionResponseDto.class);
        if (getPositionResponseDto.getData().isEmpty()) {
            System.out.println("Abe dekh kya raha hai trade utha na");
        }
        System.out.println(getPositionResponseDto);
        return getPositionResponseDto;
    }
}
