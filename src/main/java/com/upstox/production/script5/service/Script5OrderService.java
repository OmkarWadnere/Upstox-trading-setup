package com.upstox.production.script5.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.upstox.production.centralconfiguration.dto.GetPositionDataDto;
import com.upstox.production.centralconfiguration.dto.GetPositionResponseDto;
import com.upstox.production.centralconfiguration.dto.OrderRequestDto;
import com.upstox.production.centralconfiguration.entity.UpstoxLogin;
import com.upstox.production.centralconfiguration.excpetion.UpstoxException;
import com.upstox.production.centralconfiguration.repository.UpstoxLoginRepository;
import com.upstox.production.script5.entity.Script5FutureMapping;
import com.upstox.production.script5.entity.Script5NextFutureMapping;
import com.upstox.production.script5.entity.Script5OrderMapper;
import com.upstox.production.script5.repository.Script5FutureMappingRepository;
import com.upstox.production.script5.repository.Script5NextFutureMapperRepository;
import com.upstox.production.script5.repository.Script5OrderMapperRepository;
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
public class Script5OrderService {

    private static final Log log = LogFactory.getLog(Script5OrderService.class);

    private static final Double MULTIPLIER = 0.05;

    @Autowired
    private Environment environment;

    @Autowired
    private UpstoxLoginRepository upstoxLoginRepository;

    @Autowired
    private Script5FutureMappingRepository script5FutureMappingRepository;

    @Autowired
    private Script5OrderMapperRepository script5OrderMapperRepository;

    @Autowired
    private Script5NextFutureMapperRepository script5NextFutureMapperRepository;

    public String buyOrderExecution(String requestData) throws UpstoxException, IOException, InterruptedException, UnirestException {

        // Process buy order Request Data
        OrderRequestDto orderRequestDto = processBuyOrderRequestData(requestData);

        //get login details
        UpstoxLogin upstoxLogin = getLoginDetails();
        String token = "Bearer " + upstoxLogin.getAccess_token();

        log.info("The token we are using for login for today's session is : " + token);

        //place new order
        String orderDetails = buyOrderProcess(token, orderRequestDto);

        //update future mapper based on expiry date
        updateFutureMapper(orderRequestDto);

        // Put New Entry order
        return orderDetails;

    }


    public String sellOrderExecution(String requestData) throws UpstoxException, IOException, InterruptedException, UnirestException {

        // Process buy order Request Data
        OrderRequestDto orderRequestDto = processSellOrderRequestData(requestData);

        //get login details
        UpstoxLogin upstoxLogin = getLoginDetails();
        String token = "Bearer " + upstoxLogin.getAccess_token();

        log.info("The token we are using for login for today's session is : " + token);

        //place new order
        String orderDetails = sellOrderEntry(token, orderRequestDto);

        //update future mapper based on expiry date
        updateFutureMapper(orderRequestDto);

        // Put New Entry order
        return orderDetails;

    }

    public void updateFutureMapper(OrderRequestDto orderRequestDto) throws UpstoxException {
        Script5FutureMapping futureMapping = getFutureMapping(orderRequestDto);
        LocalTime twelvePM = LocalTime.of(12, 0);
        if (futureMapping.getExpiryDate().equals(LocalDate.now()) && (LocalTime.now().isAfter(twelvePM) || LocalTime.now().equals(twelvePM))) {
            Optional<Script5NextFutureMapping> nextFutureMappingsOptional = script5NextFutureMapperRepository.findBySymbolName(orderRequestDto.getInstrument_name());
            log.info("Here we came");
            if (nextFutureMappingsOptional.isEmpty()) {
                log.info("Future mapping has be already done or you forgot to add upcoming future mapping please check by adding nextFutureMapping the date you are expecting");
                return;
            }
            log.info("Next Future data recived : " + nextFutureMappingsOptional.get());
            script5FutureMappingRepository.deleteById(futureMapping.getId());

            Script5FutureMapping futureMappingToNextExpiryAfter12Pm = Script5FutureMapping.builder()
                    .expiryDate(nextFutureMappingsOptional.get().getExpiryDate())
                    .symbolName(nextFutureMappingsOptional.get().getSymbolName())
                    .instrumentToken(nextFutureMappingsOptional.get().getInstrumentToken())
                    .quantity(nextFutureMappingsOptional.get().getQuantity()).build();
            futureMappingToNextExpiryAfter12Pm = script5FutureMappingRepository.save(futureMappingToNextExpiryAfter12Pm);
            script5NextFutureMapperRepository.deleteById(nextFutureMappingsOptional.get().getId());
            log.info("The current future expiry has been added successfully !! " + futureMappingToNextExpiryAfter12Pm);
        }
    }


    public String buyOrderProcess(String token, OrderRequestDto orderRequestDto) throws UpstoxException, IOException, InterruptedException, UnirestException {

        log.info("Placing the new Entry order for : " + orderRequestDto);
        HttpResponse<String> receiveNewOrderResponse = null;
        Script5FutureMapping futureMapping = getFutureMapping(orderRequestDto);

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
            log.info("Market order sent for BUY entry : " + receiveNewOrderResponse.body());
        }
        if (orderRequestDto.getTransaction_type().equalsIgnoreCase("SELL")) {
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
                log.info("Market order sent for SELL means previous square off : " + receiveNewOrderResponse.body());
            } else {
                log.info("There is no existing buy position to square off the trade at time : " + LocalDateTime.now());
            }
        }

        if (receiveNewOrderResponse == null) {
            throw new UpstoxException("There is some error in placing order in script1 of buy order execution");
        }

        return receiveNewOrderResponse.body();
    }

    public String sellOrderEntry(String token, OrderRequestDto orderRequestDto) throws UpstoxException, IOException, InterruptedException, UnirestException {

        log.info("Placing the new Entry order for : " + orderRequestDto);
        HttpResponse<String> receiveNewOrderResponse = null;
        Script5FutureMapping futureMapping = getFutureMapping(orderRequestDto);

        if (orderRequestDto.getTransaction_type().equalsIgnoreCase("BUY")) {
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
        }

        if (receiveNewOrderResponse == null) {
            throw new UpstoxException("There is some error in placing order in script1 of buy order execution");
        }

        return receiveNewOrderResponse.body();
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
        Script5FutureMapping futureMapping = getFutureMapping(orderRequestDto);

        for (GetPositionDataDto positionDataDto : getPositionResponseDto.getData()) {
            if (positionDataDto.getInstrumentToken().equalsIgnoreCase(futureMapping.getInstrumentToken()) && positionDataDto.getQuantity() !=0) {
                return true;
            }
        }
        return false;
    }

    public Script5FutureMapping getFutureMapping(OrderRequestDto orderRequestDto) throws UpstoxException {
        Optional<Script5FutureMapping> optionalFutureMappingSymbolName = script5FutureMappingRepository.findBySymbolName(orderRequestDto.getInstrument_name());
        return optionalFutureMappingSymbolName.orElseThrow(() -> new UpstoxException("The provided Symbol is not available in future mappping Database " + orderRequestDto.getInstrument_name()));
    }

    public GetPositionResponseDto getAllPositionCall(String token) throws UnirestException, JsonProcessingException {
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


    public UpstoxLogin getLoginDetails() throws UpstoxException {
        log.info("Find the user is available in our DB for provided email Id");
        Optional<UpstoxLogin> optionalUpstoxLogin = upstoxLoginRepository.findByEmail(environment.getProperty("email_id"));
        if (optionalUpstoxLogin.isEmpty()) {
            throw new UpstoxException("Currently we don't have any user information who has logged in!!");
        }
        log.info("User Logged In is : " + optionalUpstoxLogin.get());
        return optionalUpstoxLogin.get();
    }


   public  OrderRequestDto processBuyOrderRequestData(String requestData) throws JsonProcessingException {
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
               .price(Double.parseDouble(map.get("entryPrice")))
               .instrument_name(instrumentName)
               .order_type("LIMIT")
               .transaction_type(map.get("TYPE").trim().equals("LE") ? "BUY" : "SELL")
               .entryPrice(Double.parseDouble(map.get("entryPrice")))
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
                .price(Double.parseDouble(map.get("entryPrice")))
                .instrument_name(instrumentName)
                .order_type("LIMIT")
                .transaction_type(map.get("TYPE").trim().equals("SE") ? "SELL" : "BUY")
                .entryPrice(Double.parseDouble(map.get("entryPrice")))
                .build();
    }

    public static List<Script5OrderMapper> convertIterableToListOrderMapper(Iterable<Script5OrderMapper> iterable) {
        List<Script5OrderMapper> list = new ArrayList<>();

        for (Script5OrderMapper item : iterable) {
            list.add(item);
        }

        return list;
    }

    public static List<Script5NextFutureMapping> convertIterableToListNextFutureMapper(Iterable<Script5NextFutureMapping> iterable) {
        List<Script5NextFutureMapping> list = new ArrayList<>();

        for (Script5NextFutureMapping item : iterable) {
            list.add(item);
        }

        return list;
    }

    public static List<Script5FutureMapping> convertIterableToListFutureMapper(Iterable<Script5FutureMapping> iterable) {
        List<Script5FutureMapping> list = new ArrayList<>();

        for (Script5FutureMapping item : iterable) {
            list.add(item);
        }

        return list;
    }
}
