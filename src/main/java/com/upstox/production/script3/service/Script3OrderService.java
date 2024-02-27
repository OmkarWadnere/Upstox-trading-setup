package com.upstox.production.script3.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.upstox.production.centralconfiguration.dto.*;
import com.upstox.production.centralconfiguration.entity.UpstoxLogin;
import com.upstox.production.centralconfiguration.excpetion.UpstoxException;
import com.upstox.production.centralconfiguration.repository.UpstoxLoginRepository;
import com.upstox.production.script3.entity.Script3FutureMapping;
import com.upstox.production.script3.entity.Script3NextFutureMapping;
import com.upstox.production.script3.entity.Script3OrderMapper;
import com.upstox.production.script3.repository.Script3FutureMappingRepository;
import com.upstox.production.script3.repository.Script3NextFutureMapperRepository;
import com.upstox.production.script3.repository.Script3OrderMapperRepository;
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
import java.time.LocalTime;
import java.util.*;

@Service
@PropertySource("classpath:data.properties")
public class Script3OrderService {

    private static final Log log = LogFactory.getLog(Script3OrderService.class);

    private static final Double MULTIPLIER = 0.05;

    @Autowired
    private Environment environment;

    @Autowired
    private UpstoxLoginRepository upstoxLoginRepository;

    @Autowired
    private Script3FutureMappingRepository script3FutureMappingRepository;

    @Autowired
    private Script3OrderMapperRepository script3OrderMapperRepository;

    @Autowired
    private Script3NextFutureMapperRepository script3NextFutureMapperRepository;

    public String buyOrderExecution(String requestData) throws UpstoxException, IOException, InterruptedException, UnirestException {

        // Process buy order Request Data
        OrderRequestDto orderRequestDto = processBuyOrderRequestData(requestData);

        //get login details
        UpstoxLogin upstoxLogin = getLoginDetails();
        String token = "Bearer " + upstoxLogin.getAccess_token();

        log.info("The token we are using for login for today's session is : " + token);

        //cancel all previous order
        cancelAllPreviousOrder(token);

        //squareOff existing banknifty position
        squareOffExisitngBankNiftyPosition(token, orderRequestDto);

        //update future mapper based on expiry date
        updateFutureMapper(orderRequestDto);

        // Put New Entry order
        return placeNewOrder(token, orderRequestDto);

    }

    public void updateFutureMapper(OrderRequestDto orderRequestDto) throws UpstoxException {
        Script3FutureMapping futureMapping = getFutureMapping(orderRequestDto);
        LocalTime twelvePM = LocalTime.of(12, 0);
        if (futureMapping.getExpiryDate().equals(LocalDate.now()) && (LocalTime.now().isAfter(twelvePM) || LocalTime.now().equals(twelvePM))) {
            Optional<Script3NextFutureMapping> nextFutureMappingsOptional = script3NextFutureMapperRepository.findBySymbolName(orderRequestDto.getInstrument_name());
            log.info("Here we came");
            if (nextFutureMappingsOptional.isEmpty()) {
                log.info("Future mapping has be already done or you forgot to add upcoming future mapping please check by adding nextFutureMapping the date you are expecting");
                return;
            }
            log.info("Next Future data recived : " + nextFutureMappingsOptional.get());
            script3FutureMappingRepository.deleteById(futureMapping.getId());

            Script3FutureMapping futureMappingToNextExpiryAfter12Pm = Script3FutureMapping.builder()
                    .expiryDate(nextFutureMappingsOptional.get().getExpiryDate())
                    .symbolName(nextFutureMappingsOptional.get().getSymbolName())
                    .instrumentToken(nextFutureMappingsOptional.get().getInstrumentToken())
                    .quantity(nextFutureMappingsOptional.get().getQuantity()).build();
            futureMappingToNextExpiryAfter12Pm = script3FutureMappingRepository.save(futureMappingToNextExpiryAfter12Pm);
            script3NextFutureMapperRepository.deleteById(nextFutureMappingsOptional.get().getId());
            log.info("The current future expiry has been added successfully !! " + futureMappingToNextExpiryAfter12Pm);
        }
    }

    public void squareOffExisitngBankNiftyPosition(String token, OrderRequestDto orderRequestDto) throws UnirestException, IOException, UpstoxException, InterruptedException {

        //get All positions
        GetPositionResponseDto getPositionResponseDto = getAllPostionCall(token);

        if (!getPositionResponseDto.getStatus().equalsIgnoreCase("success")) {
            throw new UpstoxException("We are not getting response for get positions from upstox its time to take manual action!!");
        }

        if (getPositionResponseDto.getData().isEmpty()) {
            return;
        }

        // Check particular symbol is available in futureMapping or not
        Script3FutureMapping futureMapping = getFutureMapping(orderRequestDto);

        for (GetPositionDataDto positionDataDto : getPositionResponseDto.getData()) {
            if (positionDataDto.getInstrumentToken().equalsIgnoreCase(futureMapping.getInstrumentToken())) {
                if (orderRequestDto.getTransaction_type().equalsIgnoreCase("BUY") && positionDataDto.getQuantity() != 0) {
                    log.info("Exiting from previous SELL trade order : " + orderRequestDto);
                    String transaction_type = orderRequestDto.getTransaction_type();
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
                            .header("Authorization", token)
                            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                            .build();
                    HttpResponse<String> orderSentResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                    log.info("Market order sent for BUY means previous square off : " + orderSentResponse.body());

                } else if (orderRequestDto.getTransaction_type().equalsIgnoreCase("SELL") && positionDataDto.getQuantity() != 0) {
                    log.info("Exiting from previous BUY trade order : " + orderRequestDto);
                    String transaction_type = orderRequestDto.getTransaction_type();
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
                            .header("Authorization", token)
                            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                            .build();
                    HttpResponse<String> orderSentResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                    log.info("Market order sent for SELL means previous square off : " + orderSentResponse.body());
                }
            }
        }
    }

    public String placeNewOrder(String token, OrderRequestDto orderRequestDto) throws UpstoxException, IOException, InterruptedException {

        log.info("Plcing the new Entry order for : " + orderRequestDto);

        Script3FutureMapping futureMapping = getFutureMapping(orderRequestDto);
        Double receivedEntryPrice = orderRequestDto.getEntryPrice();
        double placeOrderEntryPrice = Math.round(receivedEntryPrice / MULTIPLIER) * MULTIPLIER;
        String requestBody = "{"
                + "\"quantity\": "+ futureMapping.getQuantity() + ","
                + "\"product\": \"D\","
                + "\"validity\": \"DAY\","
                + "\"price\": "+ placeOrderEntryPrice + ","
                + "\"tag\": \"string\","
                + "\"instrument_token\": \"" + futureMapping.getInstrumentToken() + "\","
                + "\"order_type\": \"LIMIT\","
                + "\"transaction_type\": \"" + orderRequestDto.getTransaction_type() + "\","
                + "\"disclosed_quantity\": 0,"
                + "\"trigger_price\": " + placeOrderEntryPrice + ","
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

        // Print the response status code and body
        log.info("Response Code received from server after placing new order : " + receiveNewOrderResponse.statusCode());
        log.info("Response Body received from server after placing new order: " + receiveNewOrderResponse.body());
        ObjectMapper objectMapper = new ObjectMapper();
        OrderResponse orderResponse = objectMapper.readValue(receiveNewOrderResponse.body(), OrderResponse.class);
        Script3OrderMapper script3OrderMapper = Script3OrderMapper.builder().orderId(orderResponse.getData().getOrderId()).build();
        script3OrderMapperRepository.deleteAll();
        return script3OrderMapperRepository.save(script3OrderMapper).toString();
    }

    public void cancelAllPreviousOrder(String token) throws IOException, InterruptedException, UnirestException, UpstoxException {
        Iterable<Script3OrderMapper> orderMapperIterable = script3OrderMapperRepository.findAll();
        List<Script3OrderMapper> script3OrderMappers = convertIterableToListOrderMapper(orderMapperIterable);
        log.info("The order we are trying to cancel is : " + script3OrderMappers.toString());

        if (script3OrderMappers.isEmpty()) {
            log.info("There is no order available in our DB");
            return;
        }

        for (Script3OrderMapper script3OrderMapper : script3OrderMappers)
        {
            //get order status first then cancel
            log.info("fetch the order status which we want to cancel : " + script3OrderMapper);
            String orderDetailsUrl = environment.getProperty("upstox_url") + environment.getProperty("order_details") + script3OrderMapper.getOrderId();

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
                script3OrderMapperRepository.deleteById(script3OrderMapper.getId());
                continue;
            }

            OrderData orderData = objectMapper.readValue(orderDetailsResponse.getBody(), OrderData.class);
            log.info("Received order data converted to object"+ orderData.toString());

            if (!orderData.getStatus().equalsIgnoreCase("success")) {
                throw new UpstoxException("There is some error in getting order details");
            }

            if (!orderData.getData().getOrderStatus().equalsIgnoreCase("complete")) {
                log.info("Cancelling the order for order id is : " + script3OrderMapper.getOrderId());
                String cancelOrderUrl = environment.getProperty("upstox_url") + environment.getProperty("cancel_order") + script3OrderMapper.getOrderId();
                //Unirest.setTimeouts(0, 0);
                com.mashape.unirest.http.HttpResponse<String> orderCancelResponse = Unirest.delete(cancelOrderUrl)
                        .header("Accept", "application/json")
                        .header("Authorization", token)
                        .asString();
                log.info("The order status we have received to cancel order is : " + orderCancelResponse.getBody());
                JsonNode jsonNode = objectMapper.readTree(orderCancelResponse.getBody());
                String status = jsonNode.get("status").asText();
                if (status.equalsIgnoreCase("error")) {
                    log.info("Cancel of already cancelled/rejected/completed order is not allowed");
                    script3OrderMapperRepository.deleteById(script3OrderMapper.getId());
                    continue;
                }
                log.info("We are cacelling the order for the order details : "+ orderCancelResponse.getBody());
                OrderResponse orderResponse = objectMapper.readValue(orderCancelResponse.getBody(), OrderResponse.class);

                if (orderResponse.getStatus().equalsIgnoreCase("success")) {
                    script3OrderMapperRepository.deleteById(script3OrderMapper.getId());
                } else {
                    script3OrderMapperRepository.deleteById(script3OrderMapper.getId());
                    log.error("There is some error to cancel order can you please check manually!!");
                }
                script3OrderMapperRepository.deleteById(script3OrderMapper.getId());
                log.info("Response Code of order cancel : " + orderCancelResponse.getStatus());
                log.info("Response Body of order cancel : " + orderCancelResponse.getBody());
            }

        }
    }

    public Script3FutureMapping getFutureMapping(OrderRequestDto orderRequestDto) throws UpstoxException {
        Optional<Script3FutureMapping> optionalFutureMappingSymbolName = script3FutureMappingRepository.findBySymbolName(orderRequestDto.getInstrument_name());
        return optionalFutureMappingSymbolName.orElseThrow(() -> new UpstoxException("The provided Symbol is not available in future mappping Database " + orderRequestDto.getInstrument_name()));
    }

    public GetPositionResponseDto getAllPostionCall(String token) throws UnirestException, JsonProcessingException {
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

    public static List<Script3OrderMapper> convertIterableToListOrderMapper(Iterable<Script3OrderMapper> iterable) {
        List<Script3OrderMapper> list = new ArrayList<>();

        for (Script3OrderMapper item : iterable) {
            list.add(item);
        }

        return list;
    }

    public static List<Script3NextFutureMapping> convertIterableToListNextFutureMapper(Iterable<Script3NextFutureMapping> iterable) {
        List<Script3NextFutureMapping> list = new ArrayList<>();

        for (Script3NextFutureMapping item : iterable) {
            list.add(item);
        }

        return list;
    }

    public static List<Script3FutureMapping> convertIterableToListFutureMapper(Iterable<Script3FutureMapping> iterable) {
        List<Script3FutureMapping> list = new ArrayList<>();

        for (Script3FutureMapping item : iterable) {
            list.add(item);
        }

        return list;
    }

}