package com.upstox.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.upstox.production.centralconfiguration.dto.OrderResponse;
import com.upstox.production.centralconfiguration.dto.TradeDetails;
import com.upstox.production.centralconfiguration.dto.TradeResponse;
import com.upstox.production.centralconfiguration.repository.UpstoxLoginRepository;
import com.upstox.production.script1.entity.Script1FutureMapping;
import com.upstox.production.script1.repository.Script1FutureMappingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/getHoldings")
@Slf4j
public class GetHoldingTest {

    @Autowired
    private UpstoxLoginRepository upstoxLoginRepository;

    @Autowired
    private Environment environment;

    @Autowired
    private Script1FutureMappingRepository script1FutureMappingRepository;

    private static List<Script1FutureMapping> convertIterableToListFutureMapper(Iterable<Script1FutureMapping> iterable) {
        List<Script1FutureMapping> list = new ArrayList<>();

        for (Script1FutureMapping item : iterable) {
            list.add(item);
        }

        return list;
    }

    @GetMapping("/get")
    @ResponseStatus(HttpStatus.OK)
    public TradeResponse getHoldings() throws UnirestException, JsonProcessingException {
        String token = "Bearer eyJ0eXAiOiJKV1QiLCJrZXlfaWQiOiJza192MS4wIiwiYWxnIjoiSFMyNTYifQ.eyJzdWIiOiI2TUFFQksiLCJqdGkiOiI2NjBjY2EyZWFhNmFmYTU5MGVkYzU3ZDciLCJpc011bHRpQ2xpZW50IjpmYWxzZSwiaXNBY3RpdmUiOnRydWUsInNjb3BlIjpbImludGVyYWN0aXZlIiwiaGlzdG9yaWNhbCJdLCJpYXQiOjE3MTIxMTQyMjIsImlzcyI6InVkYXBpLWdhdGV3YXktc2VydmljZSIsImV4cCI6MTcxMjE4MTYwMH0.kJmNBYnRKAMZpyhs6rc_D0QXkgf1UqqKWqVa3P4Qbz4";
        com.mashape.unirest.http.HttpResponse<String> response = Unirest.get("https://api.upstox.com/v2/order/retrieve-all")
                .header("Accept", "application/json")
                .header("Authorization", token)
                .asString();
        System.out.println(response.getBody());
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNodeOrderDetails = objectMapper.readTree(response.getBody());
        String statusOrderDetails = jsonNodeOrderDetails.get("status").asText();
        if (statusOrderDetails.equalsIgnoreCase("error")) {
            log.info("There is no order book available for today or there is some error in fetching the order book");
            return new TradeResponse();
        }
        System.out.println(response.getBody());
        TradeResponse tradeResponse = objectMapper.readValue(response.getBody(), TradeResponse.class);

        for (TradeDetails tradeDetails : tradeResponse.getData()) {
            Iterable<Script1FutureMapping> futureMappingIterable = script1FutureMappingRepository.findAll();
            List<Script1FutureMapping> script1FutureMappings = convertIterableToListFutureMapper(futureMappingIterable);
            for (Script1FutureMapping script1FutureMapping : script1FutureMappings) {
                if (tradeDetails.getTradingsymbol().contains(script1FutureMapping.getScriptName()) && tradeDetails.getStatus().equalsIgnoreCase("open")) {
                    String cancelOrderUrl = environment.getProperty("upstox_url") + environment.getProperty("cancel_order") + tradeDetails.getOrderId();

                    com.mashape.unirest.http.HttpResponse<String> orderCancelResponse = Unirest.delete(cancelOrderUrl)
                            .header("Accept", "application/json")
                            .header("Authorization", token)
                            .asString();

                    log.info("The order status we have received to cancel order is : " + orderCancelResponse.getBody());
                    JsonNode jsonNode = objectMapper.readTree(orderCancelResponse.getBody());
                    String status = jsonNode.get("status").asText();
                    if (status.equalsIgnoreCase("error")) {
                        log.info("Cancel of already cancelled/rejected/completed order is not allowed");
                        continue;
                    }
                    log.info("We are cancelling the order for the order details : " + orderCancelResponse.getBody());
                    OrderResponse orderResponse = objectMapper.readValue(orderCancelResponse.getBody(), OrderResponse.class);

//                    if (orderResponse.getStatus().equalsIgnoreCase("success")) {
//                        script1TargetOrderMapperRepository.deleteById(script1TargetOrderMapper.getId());
//                    } else {
//                        script1TargetOrderMapperRepository.deleteById(script1TargetOrderMapper.getId());
//                        log.error("There is some error to cancel order can you please check manually!!");
//                    }
//                    script1TargetOrderMapperRepository.deleteById(script1TargetOrderMapper.getId());
                    log.info("Response Code of order cancel : " + orderCancelResponse.getStatus());
                    log.info("Response Body of order cancel : " + orderCancelResponse.getBody());
                }
            }

        }
        return new TradeResponse();
    }
}
