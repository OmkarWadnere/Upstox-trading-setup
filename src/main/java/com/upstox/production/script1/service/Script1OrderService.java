package com.upstox.production.script1.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.upstox.production.centralconfiguration.dto.*;
import com.upstox.production.centralconfiguration.entity.UpstoxLogin;
import com.upstox.production.centralconfiguration.excpetion.UpstoxException;
import com.upstox.production.centralconfiguration.repository.UpstoxLoginRepository;
import com.upstox.production.script1.entity.*;
import com.upstox.production.script1.repository.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
@PropertySource("classpath:data.properties")
public class Script1OrderService {

    private static final Log log = LogFactory.getLog(Script1OrderService.class);

    private static final Double MULTIPLIER = 0.05;

    @Autowired
    private Environment environment;

    @Autowired
    private UpstoxLoginRepository upstoxLoginRepository;

    @Autowired
    private Script1FutureMappingRepository script1FutureMappingRepository;

    @Autowired
    private Script1OrderMapperRepository script1OrderMapperRepository;

    @Autowired
    private Script1NextFutureMapperRepository script1NextFutureMapperRepository;
    @Autowired
    private Script1TargetOrderMapperRepository script1TargetOrderMapperRepository;
    @Autowired
    private Script1ScheduleOrderMapperRepository script1ScheduleOrderMapperRepository;
    @Autowired
    ObjectMapper objectMapper;

    public PlacedOrderDetails buyOrderExecution(String requestData) throws UpstoxException, IOException, InterruptedException, UnirestException {

        // Process buy order Request Data
        OrderRequestDto orderRequestDto = processBuyOrderRequestData(requestData);

        //get login details
        UpstoxLogin upstoxLogin = getLoginDetails();
        String token = "Bearer " + upstoxLogin.getAccess_token();

        log.info("The token we are using for login for today's session is : " + token);

        //place new order
        PlacedOrderDetails placedOrderDetails = buyOrderProcess(token, orderRequestDto);

        log.info("Received order details : " + placedOrderDetails);

        //update future mapper based on expiry date
        updateFutureMapper(orderRequestDto);

        // Put New Entry order
        return placedOrderDetails;

    }


    public PlacedOrderDetails sellOrderExecution(String requestData) throws UpstoxException, IOException, InterruptedException, UnirestException {

        // Process sell order Request Data
        OrderRequestDto orderRequestDto = processSellOrderRequestData(requestData);

        //get login details
        UpstoxLogin upstoxLogin = getLoginDetails();
        String token = "Bearer " + upstoxLogin.getAccess_token();

        log.info("The token we are using for login for today's session is : " + token);

        //place new order
        PlacedOrderDetails placedOrderDetails = sellOrderProcess(token, orderRequestDto);

        //update future mapper based on expiry date
        updateFutureMapper(orderRequestDto);

        // Put New Entry order
        return placedOrderDetails;

    }

    public void updateFutureMapper(OrderRequestDto orderRequestDto) throws UpstoxException {
        Script1FutureMapping futureMapping = getFutureMapping(orderRequestDto);
        LocalTime twelvePM = LocalTime.of(12, 0);
        if (futureMapping.getExpiryDate().equals(LocalDate.now()) && (LocalTime.now().isAfter(twelvePM) || LocalTime.now().equals(twelvePM))) {
            Optional<Script1NextFutureMapping> nextFutureMappingsOptional = script1NextFutureMapperRepository.findBySymbolName(orderRequestDto.getInstrument_name());
            log.info("Here we came");
            if (nextFutureMappingsOptional.isEmpty()) {
                log.info("Future mapping has be already done or you forgot to add upcoming future mapping please check by adding nextFutureMapping the date you are expecting");
                return;
            }
            log.info("Next Future data recived : " + nextFutureMappingsOptional.get());
            script1FutureMappingRepository.deleteById(futureMapping.getId());

            Script1FutureMapping futureMappingToNextExpiryAfter12Pm = Script1FutureMapping.builder()
                    .expiryDate(nextFutureMappingsOptional.get().getExpiryDate())
                    .symbolName(nextFutureMappingsOptional.get().getSymbolName())
                    .instrumentToken(nextFutureMappingsOptional.get().getInstrumentToken())
                    .quantity(nextFutureMappingsOptional.get().getQuantity()).build();
            futureMappingToNextExpiryAfter12Pm = script1FutureMappingRepository.save(futureMappingToNextExpiryAfter12Pm);
            script1NextFutureMapperRepository.deleteById(nextFutureMappingsOptional.get().getId());
            log.info("The current future expiry has been added successfully !! " + futureMappingToNextExpiryAfter12Pm);
        }
    }


    private PlacedOrderDetails buyOrderProcess(String token, OrderRequestDto orderRequestDto) throws UpstoxException, IOException, InterruptedException, UnirestException {

        log.info("Placing the new Entry order for : " + orderRequestDto);
        HttpResponse<String> receiveNewOrderResponse = null;
        Script1FutureMapping futureMapping = getFutureMapping(orderRequestDto);
        PlacedOrderDetails placedOrderDetails = null;
        if (orderRequestDto.getTransaction_type().equalsIgnoreCase("BUY")) {
            String requestBody = "{"
                    + "\"quantity\": " + futureMapping.getQuantity() + ","
                    + "\"product\": \"D\","
                    + "\"validity\": \"DAY\","
                    + "\"price\": 0,"
                    + "\"tag\": \"string\","
                    + "\"instrument_token\": \"" + futureMapping.getInstrumentToken() + "\","
                    + "\"order_type\": \"MARKET\","
                    + "\"transaction_type\": \"" + orderRequestDto.getTransaction_type() + "\","
                    + "\"disclosed_quantity\": 0,"
                    + "\"trigger_price\": 0,"
                    + "\"is_amo\": false"
                    + "}";

            // Create the HttpRequest
            HttpClient httpClient = HttpClient.newHttpClient();
            // Create the HttpRequest
            String orderUrl = environment.getProperty("upstox_url") + environment.getProperty("place_order");
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(orderUrl))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", token)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
            receiveNewOrderResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            placedOrderDetails = objectMapper.readValue(receiveNewOrderResponse.body(), PlacedOrderDetails.class);
            log.info("Complete details of received order response is : " + placedOrderDetails);

            // Get BUY order details
            OrderData orderData = getOrderDetails(orderRequestDto, placedOrderDetails, token);

            // Calculate 1:2 risk to reward ratio
            double targetPrice = calculateRiskToRewardRatioForBuy(orderRequestDto, orderData, token);

            // Place Limit target order
            placeBuyTargetOrder(orderRequestDto, orderData, targetPrice, token);

            log.info("Market order sent for BUY entry : " + receiveNewOrderResponse.body());
        }
        if (orderRequestDto.getTransaction_type().equalsIgnoreCase("SELL")) {
            cancelAllTargetOrder(token);
            script1TargetOrderMapperRepository.deleteAll();
            if (detailsOfExistingPosition(token, orderRequestDto)) {
                String requestBody = "{"
                        + "\"quantity\": " + futureMapping.getQuantity() + ","
                        + "\"product\": \"D\","
                        + "\"validity\": \"DAY\","
                        + "\"price\": 0,"
                        + "\"tag\": \"string\","
                        + "\"instrument_token\": \"" + futureMapping.getInstrumentToken() + "\","
                        + "\"order_type\": \"MARKET\","
                        + "\"transaction_type\": \"" + orderRequestDto.getTransaction_type() + "\","
                        + "\"disclosed_quantity\": 0,"
                        + "\"trigger_price\": 0,"
                        + "\"is_amo\": false"
                        + "}";

                // Create the HttpRequest
                HttpClient httpClient = HttpClient.newHttpClient();
                // Create the HttpRequest
                String orderUrl = environment.getProperty("upstox_url") + environment.getProperty("place_order");
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(orderUrl))
                        .header("Content-Type", "application/json")
                        .header("Accept", "application/json")
                        .header("Authorization", token)
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .build();
                receiveNewOrderResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                ObjectMapper objectMapper = new ObjectMapper();
                placedOrderDetails = objectMapper.readValue(receiveNewOrderResponse.body(), PlacedOrderDetails.class);
                log.info("Complete details of received order response is : " + placedOrderDetails);

                log.info("Market order sent for SELL means previous BUY square off : " + receiveNewOrderResponse.body());
            } else {
                log.info("There is no existing buy position to square off the trade at time : " + LocalDateTime.now() + " for instrument details : " + orderRequestDto);
            }
        }

        if (receiveNewOrderResponse == null) {
            throw new UpstoxException("There is some error in placing order in script1 of buy order execution");
        }

        return placedOrderDetails;
    }

    public PlacedOrderDetails sellOrderProcess(String token, OrderRequestDto orderRequestDto) throws UpstoxException, IOException, InterruptedException, UnirestException {

        log.info("Placing the new Entry order for : " + orderRequestDto);
        HttpResponse<String> receiveNewOrderResponse = null;
        Script1FutureMapping futureMapping = getFutureMapping(orderRequestDto);
        PlacedOrderDetails placedOrderDetails = null;

        if (orderRequestDto.getTransaction_type().equalsIgnoreCase("BUY")) {
            cancelAllTargetOrder(token);
            script1TargetOrderMapperRepository.deleteAll();
            if (detailsOfExistingPosition(token, orderRequestDto)) {
                String requestBody = "{"
                        + "\"quantity\": " + futureMapping.getQuantity() + ","
                        + "\"product\": \"D\","
                        + "\"validity\": \"DAY\","
                        + "\"price\": 0,"
                        + "\"tag\": \"string\","
                        + "\"instrument_token\": \"" + futureMapping.getInstrumentToken() + "\","
                        + "\"order_type\": \"MARKET\","
                        + "\"transaction_type\": \"" + orderRequestDto.getTransaction_type() + "\","
                        + "\"disclosed_quantity\": 0,"
                        + "\"trigger_price\": 0,"
                        + "\"is_amo\": false"
                        + "}";

                // Create the HttpRequest
                HttpClient httpClient = HttpClient.newHttpClient();
                // Create the HttpRequest
                String orderUrl = environment.getProperty("upstox_url") + environment.getProperty("place_order");
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(orderUrl))
                        .header("Content-Type", "application/json")
                        .header("Accept", "application/json")
                        .header("Authorization", token)
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .build();
                receiveNewOrderResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                log.info("Market order sent for BUY means previous square off : " + receiveNewOrderResponse.body());
            }else {
                log.info("There is no existing sell position to square off the trade at time : " + LocalDateTime.now());
            }

        }
        if (orderRequestDto.getTransaction_type().equalsIgnoreCase("SELL")) {
            String requestBody = "{"
                    + "\"quantity\": " + futureMapping.getQuantity() + ","
                    + "\"product\": \"D\","
                    + "\"validity\": \"DAY\","
                    + "\"price\": 0,"
                    + "\"tag\": \"string\","
                    + "\"instrument_token\": \"" + futureMapping.getInstrumentToken() + "\","
                    + "\"order_type\": \"MARKET\","
                    + "\"transaction_type\": \"" + orderRequestDto.getTransaction_type() + "\","
                    + "\"disclosed_quantity\": 0,"
                    + "\"trigger_price\": 0,"
                    + "\"is_amo\": false"
                    + "}";

            // Create the HttpRequest
            HttpClient httpClient = HttpClient.newHttpClient();
            // Create the HttpRequest
            String orderUrl = environment.getProperty("upstox_url") + environment.getProperty("place_order");
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(orderUrl))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .header("Authorization", token)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();
            receiveNewOrderResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            log.info("Market order sent for SELL means new entry : " + receiveNewOrderResponse.body());
            placedOrderDetails = objectMapper.readValue(receiveNewOrderResponse.body(), PlacedOrderDetails.class);
            log.info("Complete details of received order response is : " + placedOrderDetails);
            // Get BUY order details
            OrderData orderData = getOrderDetails(orderRequestDto, placedOrderDetails, token);

            // Calculate 1:2 risk to reward ratio
            double targetPrice = calculateRiskToRewardRatioForSell(orderRequestDto, orderData, token);

            // Place Limit target order
            placeSellTargetOrder(orderRequestDto, orderData, targetPrice, token);

        }

        if (receiveNewOrderResponse == null) {
            throw new UpstoxException("There is some error in placing order in script1 of buy order execution");
        }

        return placedOrderDetails;
    }

    public boolean detailsOfExistingPosition(String token, OrderRequestDto orderRequestDto) throws UnirestException, IOException, UpstoxException, InterruptedException {

        //get All positions
        GetPositionResponseDto getPositionResponseDto = getAllPositionCall(token);

        if (!getPositionResponseDto.getStatus().equalsIgnoreCase("success")) {
            throw new UpstoxException("We are not getting response for get positions from upstox its time to take manual action!!");
        }

        if (getPositionResponseDto.getData().isEmpty()) {
            return false;
        }

        // Check particular symbol is available in futureMapping or not
        Script1FutureMapping futureMapping = getFutureMapping(orderRequestDto);

        for (GetPositionDataDto positionDataDto : getPositionResponseDto.getData()) {
            if (positionDataDto.getInstrumentToken().equalsIgnoreCase(futureMapping.getInstrumentToken()) && positionDataDto.getQuantity() !=0) {
                return true;
            }
        }
        return false;
    }

    private Script1FutureMapping getFutureMapping(OrderRequestDto orderRequestDto) throws UpstoxException {
        Optional<Script1FutureMapping> optionalFutureMappingSymbolName = script1FutureMappingRepository.findBySymbolName(orderRequestDto.getInstrument_name());
        return optionalFutureMappingSymbolName.orElseThrow(() -> new UpstoxException("The provided Symbol is not available in future mappping Database " + orderRequestDto.getInstrument_name()));
    }

    private GetPositionResponseDto getAllPositionCall(String token) throws UnirestException, JsonProcessingException {
        log.info("Fetch the current position we are holding");
        Unirest.setTimeouts(0, 0);
        com.mashape.unirest.http.HttpResponse<String> getAllPositionResponse = Unirest.get(environment.getProperty("upstox_url") + environment.getProperty("get_position"))
                .header("Accept", "application/json")
                .header("Authorization", token)
                .asString();

        log.info("Current positions received : " + getAllPositionResponse.getBody());
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(getAllPositionResponse.getBody(), GetPositionResponseDto.class);
    }


    private UpstoxLogin getLoginDetails() throws UpstoxException {
        log.info("Find the user is available in our DB for provided email Id");
        Optional<UpstoxLogin> optionalUpstoxLogin = upstoxLoginRepository.findByEmail(environment.getProperty("email_id"));
        if (optionalUpstoxLogin.isEmpty()) {
            throw new UpstoxException("Currently we don't have any user information who has logged in!!");
        }
        log.info("User Logged In is : " + optionalUpstoxLogin.get());
        return optionalUpstoxLogin.get();
    }


   private  OrderRequestDto processBuyOrderRequestData(String requestData) throws JsonProcessingException {
       ObjectMapper objectMapper = new ObjectMapper();
       JsonNode jsonNode = objectMapper.readTree(requestData);
        log.info("Buy Order Request process has started for the data : " + jsonNode.toString());
       // Continue with your processing
       double price = jsonNode.get("price").asDouble();
       int quantity = jsonNode.get("quantity").asInt();
       String instrumentName = jsonNode.get("instrument_name").asText();
       String orderType = jsonNode.get("order_type").asText();
       String transactionType = jsonNode.get("transaction_type").asText();

       log.info("Convert order_name into separate JsoneNode");
       // If you need to convert order_name into a separate JsonNode with key-value pairs
       String[] parts = jsonNode.get("order_name").toString().replace("\"", "").split(" ");
       Map<String, String> map = new HashMap<>();
       log.info("order_name parts : " + Arrays.toString(parts));
       for (String part : parts) {
           log.info("Single parts : " + part);
           String[] subParts = part.split(":");
           map.put(subParts[0], subParts[1]);
       }

       log.info("The type of order we have received : " + orderType);

       return OrderRequestDto.builder()
               .quantity(quantity)
               .price(Double.parseDouble(map.get("stoplossPrice")))
               .instrument_name(instrumentName)
               .order_type("LIMIT")
               .transaction_type(map.get("TYPE").trim().equals("LE") ? "BUY" : "SELL")
               .stoplossPrice(Double.parseDouble(map.get("stoplossPrice")))
               .build();
   }

    public  OrderRequestDto processSellOrderRequestData(String requestData) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(requestData);
        log.info("Buy Order Request process has started for the data : " + jsonNode.toString());
        // Continue with your processing
        double price = jsonNode.get("price").asDouble();
        int quantity = jsonNode.get("quantity").asInt();
        String instrumentName = jsonNode.get("instrument_name").asText();
        String orderType = jsonNode.get("order_type").asText();
        String transactionType = jsonNode.get("transaction_type").asText();

        log.info("Convert order_name into separate JsoneNode");
        // If you need to convert order_name into a separate JsonNode with key-value pairs
        String[] parts = jsonNode.get("order_name").toString().replace("\"", "").split(" ");
        Map<String, String> map = new HashMap<>();
        log.info("order_name parts : " + Arrays.toString(parts));
        for (String part : parts) {
            log.info("Single parts : " + part);
            String[] subParts = part.split(":");
            map.put(subParts[0], subParts[1]);
        }

        log.info("The type of order we have received : " + orderType);

        return OrderRequestDto.builder()
                .quantity(quantity)
                .price(Double.parseDouble(map.get("stoplossPrice")))
                .instrument_name(instrumentName)
                .order_type("LIMIT")
                .transaction_type(map.get("TYPE").trim().equals("SE") ? "SELL" : "BUY")
                .stoplossPrice(Double.parseDouble(map.get("stoplossPrice")))
                .build();
    }

    public void deleteAllScript1OrderMapper() {
        script1OrderMapperRepository.deleteAll();
    }

    public void deleteAllScript1ScheduleOrderMapper() {
        script1ScheduleOrderMapperRepository.deleteAll();
    }

    public void deleteAllScript1TargetOrderMapper() {
        script1TargetOrderMapperRepository.deleteAll();
    }

    private OrderData getOrderDetails(OrderRequestDto orderRequestDto, PlacedOrderDetails placedOrderDetails, String token) throws UnirestException, JsonProcessingException, UpstoxException {
        //get order status first then cancel
        String orderDetailsUrl = environment.getProperty("upstox_url") + environment.getProperty("order_details") + placedOrderDetails.getData().getOrderId();

        com.mashape.unirest.http.HttpResponse<String> orderDetailsResponse = Unirest.get(orderDetailsUrl)
                .header("Accept", "application/json")
                .header("Authorization", token)
                .asString();
        ObjectMapper objectMapper = new ObjectMapper();

        log.info("Received order status from server is : "+ orderDetailsResponse.getBody());
        JsonNode jsonNodeOrderDetails = objectMapper.readTree(orderDetailsResponse.getBody());
        String statusOrderDetails = jsonNodeOrderDetails.get("status").asText();
        if (statusOrderDetails.equalsIgnoreCase("error")) {
//            log.info("The order is of previous day so we can't perform any operation on this!!");
//            script1OrderMapperRepository.deleteById(script1OrderMapper.getId());
//            continue;
            throw new UpstoxException("There is some error in fetching the order details for order Id : " + placedOrderDetails.getData().getOrderId() + " and Order placed for the data is : " + orderRequestDto);
        }

        return objectMapper.readValue(orderDetailsResponse.getBody(), OrderData.class);
    }

    private double calculateRiskToRewardRatioForBuy(OrderRequestDto orderRequestDto, OrderData orderData, String token) {
            double entryPrice = orderData.getData().getAveragePrice();
            double stoplossPrice = orderRequestDto.getStoplossPrice();
            return entryPrice + ((entryPrice-stoplossPrice) * 2);
    }

    private double calculateRiskToRewardRatioForSell(OrderRequestDto orderRequestDto, OrderData orderData, String  token) {
        double entryPrice = orderData.getData().getPrice();
        double stoplossPrice = orderRequestDto.getStoplossPrice();
        return entryPrice - ((stoplossPrice - entryPrice)* 2);
    }

    private void placeBuyTargetOrder(OrderRequestDto orderRequestDto, OrderData orderData, double targetPrice, String token) throws UpstoxException, IOException, InterruptedException {
        log.info("Placing the new Target order for : " + orderRequestDto);
        double placeOrderTargetPrice = Math.round(targetPrice / MULTIPLIER) * MULTIPLIER;
        Script1FutureMapping futureMapping = getFutureMapping(orderRequestDto);
        Script1ScheduleOrderMapper script1ScheduleOrderMapper = Script1ScheduleOrderMapper.builder().orderType("SELL")
                .targetPrice(placeOrderTargetPrice).instrumentToken(futureMapping.getInstrumentToken()).build();
        script1ScheduleOrderMapperRepository.save(script1ScheduleOrderMapper);
        String requestBody = "{"
                + "\"quantity\": "+ orderData.getData().getQuantity() + ","
                + "\"product\": \"D\","
                + "\"validity\": \"DAY\","
                + "\"price\": "+ placeOrderTargetPrice + ","
                + "\"tag\": \"string\","
                + "\"instrument_token\": \"" + futureMapping.getInstrumentToken() + "\","
                + "\"order_type\": \"LIMIT\","
                + "\"transaction_type\": \"SELL\","
                + "\"disclosed_quantity\": 0,"
                + "\"trigger_price\": " + placeOrderTargetPrice + ","
                + "\"is_amo\": false"
                + "}";

        log.info("Request body we are sending for placing new entry order : " + requestBody);

        // Create the HttpRequest
        HttpClient httpClient = HttpClient.newHttpClient();


        String orderUrl = environment.getProperty("upstox_url") + environment.getProperty("place_order");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(orderUrl))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", token)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
        HttpResponse<String> receiveNewOrderResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        ObjectMapper objectMapper = new ObjectMapper();
        PlacedOrderDetails placedOrderDetails = objectMapper.readValue(receiveNewOrderResponse.body(), PlacedOrderDetails.class);
        if (placedOrderDetails.getStatus().equalsIgnoreCase("success")) {
            throw new UpstoxException("There is some error in placing target order for buy : " + orderRequestDto);
        }
        script1TargetOrderMapperRepository.save(Script1TargetOrderMapper.builder().orderId(placedOrderDetails.getData().getOrderId()).build());
    }

    private void placeSellTargetOrder(OrderRequestDto orderRequestDto, OrderData orderData, double targetPrice, String token) throws UpstoxException, IOException, InterruptedException {
        log.info("Placing the new Target order for : " + orderRequestDto);
        double placeOrderTargetPrice = Math.round(targetPrice / MULTIPLIER) * MULTIPLIER;
        Script1FutureMapping futureMapping = getFutureMapping(orderRequestDto);
        int quantity = (orderData.getData().getQuantity() < 0) ? (orderData.getData().getQuantity() * -1) : (orderData.getData().getQuantity());
        Script1ScheduleOrderMapper script1ScheduleOrderMapper = Script1ScheduleOrderMapper.builder().orderType("BUY")
                .targetPrice(placeOrderTargetPrice).instrumentToken(futureMapping.getInstrumentToken()).build();
        script1ScheduleOrderMapperRepository.save(script1ScheduleOrderMapper);
        String requestBody = "{"
                + "\"quantity\": "+ quantity + ","
                + "\"product\": \"D\","
                + "\"validity\": \"DAY\","
                + "\"price\": "+ placeOrderTargetPrice + ","
                + "\"tag\": \"string\","
                + "\"instrument_token\": \"" + futureMapping.getInstrumentToken() + "\","
                + "\"order_type\": \"LIMIT\","
                + "\"transaction_type\": \"BUY\","
                + "\"disclosed_quantity\": 0,"
                + "\"trigger_price\": " + placeOrderTargetPrice + ","
                + "\"is_amo\": false"
                + "}";

        log.info("Request body we are sending for placing new entry order : " + requestBody);

        // Create the HttpRequest
        HttpClient httpClient = HttpClient.newHttpClient();


        String orderUrl = environment.getProperty("upstox_url") + environment.getProperty("place_order");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(orderUrl))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", token)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
        HttpResponse<String> receiveNewOrderResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        ObjectMapper objectMapper = new ObjectMapper();
        PlacedOrderDetails placedOrderDetails = objectMapper.readValue(receiveNewOrderResponse.body(), PlacedOrderDetails.class);
        if (placedOrderDetails.getStatus().equalsIgnoreCase("success")) {
            throw new UpstoxException("There is some error in placing target order for buy : " + orderRequestDto);
        }
        script1TargetOrderMapperRepository.save(Script1TargetOrderMapper.builder().orderId(placedOrderDetails.getData().getOrderId()).build());
    }

    private void cancelAllTargetOrder(String token) throws IOException, InterruptedException, UnirestException, UpstoxException {
        Iterable<Script1TargetOrderMapper> orderMapperIterable = script1TargetOrderMapperRepository.findAll();
        List<Script1TargetOrderMapper> script1TargetOrderMappers = convertIterableToListOrderMapper(orderMapperIterable);
        log.info("The order we are trying to cancel is : " + script1TargetOrderMappers);

        if (script1TargetOrderMappers.isEmpty()) {
            log.info("There is no order available in our DB");
            return;
        }

        for (Script1TargetOrderMapper script1TargetOrderMapper : script1TargetOrderMappers)
        {
            //get order status first then cancel
            log.info("fetch the order status which we want to cancel : " + script1TargetOrderMapper);
            String orderDetailsUrl = environment.getProperty("upstox_url") + environment.getProperty("order_details") + script1TargetOrderMapper.getOrderId();

            com.mashape.unirest.http.HttpResponse<String> orderDetailsResponse = Unirest.get(orderDetailsUrl)
                    .header("Accept", "application/json")
                    .header("Authorization", token)
                    .asString();
            ObjectMapper objectMapper = new ObjectMapper();

            log.info("Received order status from server is : "+ orderDetailsResponse.getBody());
            JsonNode jsonNodeOrderDetails = objectMapper.readTree(orderDetailsResponse.getBody());
            String statusOrderDetails = jsonNodeOrderDetails.get("status").asText();
            if (statusOrderDetails.equalsIgnoreCase("error")) {
                log.info("The order is of previous day so we can't perform any operation on this!!");
                script1TargetOrderMapperRepository.deleteById(script1TargetOrderMapper.getId());
                continue;
            }

            OrderData orderData = objectMapper.readValue(orderDetailsResponse.getBody(), OrderData.class);
            log.info("Received order data converted to object"+ orderData.toString());

            if (!orderData.getStatus().equalsIgnoreCase("success")) {
                throw new UpstoxException("There is some error in getting order details");
            }

            if (!orderData.getData().getOrderStatus().equalsIgnoreCase("complete")) {
                log.info("Cancelling the order for order id is : " + script1TargetOrderMapper.getOrderId());
                String cancelOrderUrl = environment.getProperty("upstox_url") + environment.getProperty("cancel_order") + script1TargetOrderMapper.getOrderId();

                com.mashape.unirest.http.HttpResponse<String> orderCancelResponse = Unirest.delete(cancelOrderUrl)
                        .header("Accept", "application/json")
                        .header("Authorization", token)
                        .asString();
                log.info("The order status we have received to cancel order is : " + orderCancelResponse.getBody());
                JsonNode jsonNode = objectMapper.readTree(orderCancelResponse.getBody());
                String status = jsonNode.get("status").asText();
                if (status.equalsIgnoreCase("error")) {
                    log.info("Cancel of already cancelled/rejected/completed order is not allowed");
                    script1TargetOrderMapperRepository.deleteById(script1TargetOrderMapper.getId());
                    continue;
                }
                log.info("We are cancelling the order for the order details : "+ orderCancelResponse.getBody());
                OrderResponse orderResponse = objectMapper.readValue(orderCancelResponse.getBody(), OrderResponse.class);

                if (orderResponse.getStatus().equalsIgnoreCase("success")) {
                    script1TargetOrderMapperRepository.deleteById(script1TargetOrderMapper.getId());
                } else {
                    script1TargetOrderMapperRepository.deleteById(script1TargetOrderMapper.getId());
                    log.error("There is some error to cancel order can you please check manually!!");
                }
                script1TargetOrderMapperRepository.deleteById(script1TargetOrderMapper.getId());
                log.info("Response Code of order cancel : " + orderCancelResponse.getStatus());
                log.info("Response Body of order cancel : " + orderCancelResponse.getBody());
            }
        }
    }

    private static List<Script1TargetOrderMapper> convertIterableToListOrderMapper(Iterable<Script1TargetOrderMapper> iterable) {
        List<Script1TargetOrderMapper> list = new ArrayList<>();

        for (Script1TargetOrderMapper item : iterable) {
            list.add(item);
        }

        return list;
    }

    private static List<Script1NextFutureMapping> convertIterableToListNextFutureMapper(Iterable<Script1NextFutureMapping> iterable) {
        List<Script1NextFutureMapping> list = new ArrayList<>();

        for (Script1NextFutureMapping item : iterable) {
            list.add(item);
        }

        return list;
    }

    private static List<Script1FutureMapping> convertIterableToListFutureMapper(Iterable<Script1FutureMapping> iterable) {
        List<Script1FutureMapping> list = new ArrayList<>();

        for (Script1FutureMapping item : iterable) {
            list.add(item);
        }

        return list;
    }
}
