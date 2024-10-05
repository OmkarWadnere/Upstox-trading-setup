package com.upstox.production.banknifty.service.helper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.upstox.production.banknifty.dto.BankNiftyLtpResponseDTO;
import com.upstox.production.banknifty.dto.BankNiftyOptionChainDataDTO;
import com.upstox.production.banknifty.dto.BankNiftyOptionChainResponseDTO;
import com.upstox.production.banknifty.dto.BankNiftyOptionDTO;
import com.upstox.production.banknifty.entity.BankNiftyOptionMapping;
import com.upstox.production.banknifty.entity.BankNiftyOrderMapper;
import com.upstox.production.banknifty.repository.BankNiftyOptionMappingRepository;
import com.upstox.production.banknifty.repository.BankNiftyOrderMapperRepository;
import com.upstox.production.centralconfiguration.dto.*;
import com.upstox.production.centralconfiguration.excpetion.UpstoxException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.upstox.production.banknifty.utility.BankNiftyUtility.schedulerToken;

@Component
@Slf4j
public class BankNiftyOrderHelper {

    @Autowired
    private BankNiftyOptionMappingRepository bankNiftyOptionMappingRepository;
    @Autowired
    private Environment environment;
    @Autowired
    private BankNiftyOrderMapperRepository bankNiftyOrderMapperRepository;

    public BankNiftyOptionChainResponseDTO getOptionChain(BankNiftyOptionMapping bankNiftyOptionMapping, String token) throws UpstoxException, URISyntaxException, IOException, InterruptedException {
        HttpClient httpClient = HttpClient.newBuilder().build();
        String bankNifty = "NSE_INDEX%7CNifty%20Bank";
        String expiryDate = bankNiftyOptionMapping.getExpiryDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("https://api.upstox.com/v2/option/chain?instrument_key=" + bankNifty + "&expiry_date=" + expiryDate))
                .header("Accept", "application/json")
                .header("Authorization", token)
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(response.body(), BankNiftyOptionChainResponseDTO.class);
    }

    public BankNiftyOptionDTO filterBankNiftyOptionStrike(BankNiftyOptionChainResponseDTO bankNiftyOptionChainResponseDTO, String transactionType) {
        if (transactionType.equalsIgnoreCase("CALL_BUY")) {
            return bankNiftyOptionChainResponseDTO.getData().stream()
                    .parallel()
                    .map(BankNiftyOptionChainDataDTO::getCall_options)
                    .filter(Objects::nonNull)
                    .filter(callOptions -> callOptions.getMarket_data().getLtp() >= 380.00
                            && callOptions.getMarket_data().getLtp() <= 450.00)
                    .max((o1, o2) -> Double.compare(o1.getMarket_data().getLtp(), o2.getMarket_data().getLtp()))
                    .orElse(null); // Returns null if no values in range
        } else if (transactionType.equalsIgnoreCase("PUT_BUY")) {
            return bankNiftyOptionChainResponseDTO.getData().stream()
                    .parallel()
                    .map(BankNiftyOptionChainDataDTO::getPut_options)
                    .filter(Objects::nonNull)
                    .filter(putOptions -> putOptions.getMarket_data().getLtp() >= 380.00
                            && putOptions.getMarket_data().getLtp() <= 450.00)
                    .max((o1, o2) -> Double.compare(o1.getMarket_data().getLtp(), o2.getMarket_data().getLtp()))
                    .orElse(null); // Returns null if no values in range
        }
        return null;
    }

    public void placeBuyOrder(BankNiftyOptionDTO bankNiftyOptionDTO, BankNiftyOptionMapping bankNiftyOptionMapping, String token) throws IOException, InterruptedException, UnirestException {
        ObjectMapper objectMapper = new ObjectMapper();
        // place 1st market order
        String requestBody = "{"
                + "\"quantity\": " + bankNiftyOptionMapping.getQuantity() * bankNiftyOptionMapping.getNumberOfLots() + ","
                + "\"product\": \"D\","
                + "\"validity\": \"DAY\","
                + "\"price\": 0,"
                + "\"tag\": \"string\","
                + "\"instrument_token\": \"" + bankNiftyOptionDTO.getInstrument_key() + "\","
                + "\"order_type\": \"MARKET\","
                + "\"transaction_type\": \"BUY\","
                + "\"disclosed_quantity\": 0,"
                + "\"trigger_price\": 0,"
                + "\"is_amo\": false"
                + "}";
        HttpClient httpClient = HttpClient.newHttpClient();

        String orderUrl = environment.getProperty("upstox_url") + environment.getProperty("place_order");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(orderUrl))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", token)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
        HttpResponse<String> placeOptionBuyOrderResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        JsonNode jsonNodeOrderDetails = objectMapper.readTree(placeOptionBuyOrderResponse.body());
        String statusOrderDetails = jsonNodeOrderDetails.get("status").asText();
        if (statusOrderDetails.equalsIgnoreCase("error")) {
            return;
        }
        OrderData orderData = objectMapper.readValue(placeOptionBuyOrderResponse.body(), OrderData.class);
        bankNiftyOrderMapperRepository.save(BankNiftyOrderMapper.builder().orderId(orderData.getData().getOrderId()).orderType("BUY").build());

        // get average price of market order
        String orderDetailsUrl = environment.getProperty("upstox_url") + environment.getProperty("order_details") + orderData.getData().getOrderId();
        request = HttpRequest.newBuilder()
                .uri(URI.create(orderDetailsUrl))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", token)
                .build();
        HttpResponse<String> orderDetailsResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        OrderData placedMarketOrderResponse = objectMapper.readValue(orderDetailsResponse.body(), OrderData.class);
        double averagePrice = 0.00;

        // place initial target order
        if (placedMarketOrderResponse.getData().getOrderStatus().equalsIgnoreCase("complete")) {
            averagePrice = placedMarketOrderResponse.getData().getAveragePrice();
            requestBody = "{"
                    + "\"quantity\": " + placedMarketOrderResponse.getData().getQuantity() + ","
                    + "\"product\": \"D\","
                    + "\"validity\": \"DAY\","
                    + "\"price\": " + (averagePrice + bankNiftyOptionMapping.getProfitPoints()) + ","
                    + "\"tag\": \"string\","
                    + "\"instrument_token\": \"" + bankNiftyOptionDTO.getInstrument_key() + "\","
                    + "\"order_type\": \"LIMIT\","
                    + "\"transaction_type\": \"SELL\","
                    + "\"disclosed_quantity\": 0,"
                    + "\"trigger_price\": 0,"
                    + "\"is_amo\": false"
                    + "}";
            httpClient = HttpClient.newHttpClient();

            orderUrl = environment.getProperty("upstox_url") + environment.getProperty("place_order");
            request = HttpRequest.newBuilder()
                    .uri(URI.create(orderUrl))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .header("Authorization", token)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();
            placeOptionBuyOrderResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            jsonNodeOrderDetails = objectMapper.readTree(placeOptionBuyOrderResponse.body());
            statusOrderDetails = jsonNodeOrderDetails.get("status").asText();
            if (statusOrderDetails.equalsIgnoreCase("error")) {
                return;
            }
            OrderData targetorderData = objectMapper.readValue(placeOptionBuyOrderResponse.body(), OrderData.class);
            bankNiftyOrderMapperRepository.save(BankNiftyOrderMapper.builder().orderId(targetorderData.getData().getOrderId()).orderType("SELL").build());
        } else {
            Thread.sleep(1200);
            // get average price of market order
            orderDetailsUrl = environment.getProperty("upstox_url") + environment.getProperty("order_details") + orderData.getData().getOrderId();
            request = HttpRequest.newBuilder()
                    .uri(URI.create(orderDetailsUrl))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .header("Authorization", token)
                    .build();
            orderDetailsResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            placedMarketOrderResponse = objectMapper.readValue(orderDetailsResponse.body(), OrderData.class);
            if (placedMarketOrderResponse.getData().getOrderStatus().equalsIgnoreCase("complete")) {
                averagePrice = placedMarketOrderResponse.getData().getAveragePrice();
                requestBody = "{"
                        + "\"quantity\": " + placedMarketOrderResponse.getData().getQuantity() + ","
                        + "\"product\": \"D\","
                        + "\"validity\": \"DAY\","
                        + "\"price\": " + (averagePrice + bankNiftyOptionMapping.getProfitPoints()) + ","
                        + "\"tag\": \"string\","
                        + "\"instrument_token\": \"" + bankNiftyOptionDTO.getInstrument_key() + "\","
                        + "\"order_type\": \"LIMIT\","
                        + "\"transaction_type\": \"SELL\","
                        + "\"disclosed_quantity\": 0,"
                        + "\"trigger_price\": 0,"
                        + "\"is_amo\": false"
                        + "}";
                httpClient = HttpClient.newHttpClient();

                orderUrl = environment.getProperty("upstox_url") + environment.getProperty("place_order");
                request = HttpRequest.newBuilder()
                        .uri(URI.create(orderUrl))
                        .header("Content-Type", "application/json")
                        .header("Accept", "application/json")
                        .header("Authorization", token)
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .build();
                placeOptionBuyOrderResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                OrderData targetorderData = objectMapper.readValue(placeOptionBuyOrderResponse.body(), OrderData.class);
                bankNiftyOrderMapperRepository.save(BankNiftyOrderMapper.builder().orderId(targetorderData.getData().getOrderId()).orderType("SELL").build());
            }
        }

        // place average price order
        for (int i = 1; i <= bankNiftyOptionMapping.getAveragingTimes(); i++) {
            averagePrice -= bankNiftyOptionMapping.getAveragingPointInterval();
            requestBody = "{"
                    + "\"quantity\": " + bankNiftyOptionMapping.getQuantity() * bankNiftyOptionMapping.getNumberOfLots() + ","
                    + "\"product\": \"D\","
                    + "\"validity\": \"DAY\","
                    + "\"price\": "+ averagePrice + ","
                    + "\"tag\": \"string\","
                    + "\"instrument_token\": \"" + bankNiftyOptionDTO.getInstrument_key() + "\","
                    + "\"order_type\": \"LIMIT\","
                    + "\"transaction_type\": \"BUY\","
                    + "\"disclosed_quantity\": 0,"
                    + "\"trigger_price\": 0,"
                    + "\"is_amo\": false"
                    + "}";
            orderUrl = environment.getProperty("upstox_url") + environment.getProperty("place_order");
            request = HttpRequest.newBuilder()
                    .uri(URI.create(orderUrl))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .header("Authorization", token)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();
            HttpResponse<String> intervalOrderResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            jsonNodeOrderDetails = objectMapper.readTree(intervalOrderResponse.body());
            statusOrderDetails = jsonNodeOrderDetails.get("status").asText();
            if (statusOrderDetails.equalsIgnoreCase("error")) {
                continue;
            }
            OrderData intervalOrderData = objectMapper.readValue(intervalOrderResponse.body(), OrderData.class);
            bankNiftyOrderMapperRepository.save(BankNiftyOrderMapper.builder().orderId(intervalOrderData.getData().getOrderId()).orderType("BUY").build());
        }
    }

    public BankNiftyLtpResponseDTO fetchCmp(String token) throws IOException, InterruptedException {
        String bankNifty = "NSE_INDEX%7CNifty%20Bank";
        String url = "https://api.upstox.com/v2/market-quote/ltp?instrument_key=" + bankNifty;
        String acceptHeader = "application/json";
        String authorizationHeader = token;

        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", acceptHeader)
                .header("Authorization", authorizationHeader)
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(response.body(), BankNiftyLtpResponseDTO.class);
    }

    public AllOrderDetailsDto getAllOrderDetails(String token) throws IOException, InterruptedException {
        String getAllOrderDetailsUrl = "https://api.upstox.com/v2/order/retrieve-all";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getAllOrderDetailsUrl))
                .header("Accept", "application/json")
                .header("Authorization", token)
                .build();
        HttpClient httpClient = HttpClient.newBuilder().build();
        ObjectMapper objectMapper = new ObjectMapper();
        HttpResponse<String> orderDetailsResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        JsonNode jsonNodeOrderDetails = objectMapper.readTree(orderDetailsResponse.body());
        String statusOrderDetails = jsonNodeOrderDetails.get("status").asText();
        AllOrderDetailsDto allOrderDetailsDto = null;
        if (statusOrderDetails.equalsIgnoreCase("error")) {
            log.info("There is some error in fetching all orders details");
            return null;
        }
        return objectMapper.readValue(orderDetailsResponse.body(), AllOrderDetailsDto.class);
    }

    public void cancelAllOpenOrders() throws IOException, InterruptedException {
        //cancel all open orders
        int counter = 1;
        AllOrderDetailsDto allOrderDetailsDto = getAllOrderDetails(schedulerToken);
        for (OrderDetails orderDetails : allOrderDetailsDto.getData()) {
            if (orderDetails.getOrderStatus().equalsIgnoreCase("open")) {
                String url = "https://api-hft.upstox.com/v2/order/cancel?order_id=" + orderDetails.getOrderId();

                // Replace with your actual values
                String acceptHeader = "application/json";

                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("Accept", acceptHeader)
                        .header("Authorization", schedulerToken)
                        .DELETE()
                        .build();

                HttpResponse<String> cancelOrderResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNodeOrderDetails = objectMapper.readTree(cancelOrderResponse.body());
                String statusOrderDetails = jsonNodeOrderDetails.get("status").asText();
                if (statusOrderDetails.equalsIgnoreCase("error")) {
                    log.info("There is some error in placing cancel order : " + orderDetails.getOrderId());
                }
            }
        }
        bankNiftyOrderMapperRepository.deleteAll();
    }

    public void squareOffAllPositions() throws UpstoxException, UnirestException, IOException, InterruptedException {
        // get all positions and square off it
        log.info("Fetch the current position we are holding");
        Unirest.setTimeouts(0, 0);
        com.mashape.unirest.http.HttpResponse<String> getAllPositionResponse = Unirest.get(environment.getProperty("upstox_url") + environment.getProperty("get_position"))
                .header("Accept", "application/json")
                .header("Authorization", schedulerToken)
                .asString();

        log.info("Current positions received : " + getAllPositionResponse.getBody());
        ObjectMapper objectMapper = new ObjectMapper();
        GetPositionResponseDto getPositionResponseDto = objectMapper.readValue(getAllPositionResponse.getBody(), GetPositionResponseDto.class);
        if (!getPositionResponseDto.getStatus().equalsIgnoreCase("success")) {
            log.error("We are not getting response for get positions from upstox its time to take manual action!!");
            return;
        }
        for (GetPositionDataDto positionDataDto : getPositionResponseDto.getData()) {
            if (positionDataDto.getQuantity() != 0) {
                log.info("Exiting from previous trade : " + positionDataDto);
                String transaction_type = positionDataDto.getQuantity() < 0 ? "BUY" : "SELL";
                int quantity = positionDataDto.getQuantity() < 0 ? positionDataDto.getQuantity() * -1 : positionDataDto.getQuantity();
                String requestBody = "{"
                        + "\"quantity\": " + quantity + ","
                        + "\"product\": \"D\","
                        + "\"validity\": \"DAY\","
                        + "\"price\": 0,"
                        + "\"tag\": \"string\","
                        + "\"instrument_token\": \"" + positionDataDto.getInstrumentToken() + "\","
                        + "\"order_type\": \"MARKET\","
                        + "\"transaction_type\": \"" + transaction_type + "\","
                        + "\"disclosed_quantity\": 0,"
                        + "\"trigger_price\": 0,"
                        + "\"is_amo\": false"
                        + "}";

                // Create the HttpRequest
                HttpClient httpClient = HttpClient.newHttpClient();

                String orderUrl = environment.getProperty("upstox_url") + "/order/place";
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(orderUrl))
                        .header("Content-Type", "application/json")
                        .header("Accept", "application/json")
                        .header("Authorization", schedulerToken)
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .build();
                HttpResponse<String> orderSentResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                log.info("Market order sent for " + transaction_type + "means previous square off : " + orderSentResponse.body());
            }
        }
        bankNiftyOrderMapperRepository.deleteAll();
    }

    public List<Integer> getCallStrikes(double cmp) {
        List<Integer> callStrikes = new ArrayList<>();
        // Select call options below CMP
        for (int strike = (int) (cmp / 100) * 100; strike >= cmp - 300; strike -= 100) {
            if (strike < cmp) { // Ensure the strike is below CMP
                callStrikes.add(strike);
            }
        }
        return callStrikes;
    }

    public List<Integer> getPutStrikes(double cmp) {
        List<Integer> putStrikes = new ArrayList<>();
        // Select put options above CMP
        for (int strike = (int) (cmp / 100) * 100 + 100; strike <= cmp + 300; strike += 100) {
            if (strike > cmp) { // Ensure the strike is above CMP
                putStrikes.add(strike);
            }
        }
        return putStrikes;
    }
}
