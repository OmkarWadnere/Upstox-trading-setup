package com.upstox.production.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.upstox.production.Logging.LoggingAspect;
import com.upstox.production.dto.*;
import com.upstox.production.entity.FutureMapping;
import com.upstox.production.entity.NextFutureMapping;
import com.upstox.production.entity.OrderMapper;
import com.upstox.production.excpetion.UpstoxException;
import com.upstox.production.entity.UpstoxLogin;
import com.upstox.production.repository.FutureMappingRepository;
import com.upstox.production.repository.NextFutureMapperRepository;
import com.upstox.production.repository.OrderMapperRepository;
import com.upstox.production.repository.UpstoxLoginRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.logging.log4j.Logger;
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
public class BuyOrderService {

    private static final Log log = LogFactory.getLog(BuyOrderService.class);

    @Autowired
    private Environment environment;

    @Autowired
    private UpstoxLoginRepository upstoxLoginRepository;

    @Autowired
    private FutureMappingRepository futureMappingRepository;

    @Autowired
    private OrderMapperRepository orderMapperRepository;

    @Autowired
    private NextFutureMapperRepository nextFutureMapperRepository;

    public String BurOrderExecution(String requestData) throws UpstoxException, IOException, InterruptedException, UnirestException {

        // Process buy order Request Data
        OrderRequestDto orderRequestDto = processBuyOrderRequestData(requestData);

        //get login details
        UpstoxLogin upstoxLogin = getLoginDetails();
        String token = "Bearer " + upstoxLogin.getAccess_token();

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
        FutureMapping futureMapping = getFutureMapping(orderRequestDto);
        LocalTime twelvePM = LocalTime.of(12, 0);
        if (futureMapping.getExpiryDate().equals(LocalDate.now()) && (LocalTime.now().isAfter(twelvePM) || LocalTime.now().equals(twelvePM))) {
            Optional<NextFutureMapping> nextFutureMappingsOptional = nextFutureMapperRepository.findBySymbolName(orderRequestDto.getInstrument_name());

            if (nextFutureMappingsOptional.isEmpty()) {
                log.info("Future mapping has be already done or you forgot to add upcoming future mapping please check by adding nextFutureMapping the date you are expecting");
                return;
            }
            futureMappingRepository.deleteById(futureMapping.getId());

            FutureMapping futureMappingToNextExpiryAfter12Pm = FutureMapping.builder()
                    .expiryDate(nextFutureMappingsOptional.get().getExpiryDate())
                    .symbolName(nextFutureMappingsOptional.get().getSymbolName())
                    .instrumentToken(nextFutureMappingsOptional.get().getInstrumentToken())
                    .quantity(nextFutureMappingsOptional.get().getQuantity()).build();
            futureMappingToNextExpiryAfter12Pm = futureMappingRepository.save(futureMappingToNextExpiryAfter12Pm);
            nextFutureMapperRepository.deleteById(nextFutureMappingsOptional.get().getId());
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
        FutureMapping futureMapping = getFutureMapping(orderRequestDto);

        for (GetPositionDataDto positionDataDto : getPositionResponseDto.getData()) {
            if (positionDataDto.getInstrumentToken().equalsIgnoreCase(futureMapping.getInstrumentToken())) {
                if (orderRequestDto.getTransaction_type().equalsIgnoreCase("BUY")) {
                    String transaction_type = orderRequestDto.getTransaction_type();
                    System.out.println("What is transaction type here? " + transaction_type);
                    String requestBody = "{"
                            + "\"quantity\": " + positionDataDto.getQuantity() + ","
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
                    log.info("Market order sent for buy means previous square off : " + orderSentResponse.body());

                } else if (orderRequestDto.getTransaction_type().equalsIgnoreCase("SELL")) {
                    String transaction_type = orderRequestDto.getTransaction_type();
                    System.out.println("What is transaction type here? " + transaction_type);
                    String requestBody = "{"
                            + "\"quantity\": " + positionDataDto.getQuantity() + ","
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
                    log.info("Market order sent for sell means previous square off : " + orderSentResponse.body());
                }
            }
        }
    }

    public String placeNewOrder(String token, OrderRequestDto orderRequestDto) throws UpstoxException, IOException, InterruptedException {

        FutureMapping futureMapping = getFutureMapping(orderRequestDto);

        String requestBody = "{"
                + "\"quantity\": "+ orderRequestDto.getQuantity() + ","
                + "\"product\": \"D\","
                + "\"validity\": \"DAY\","
                + "\"price\": "+ orderRequestDto.getEntryPrice() + ","
                + "\"tag\": \"string\","
                + "\"instrument_token\": \"" + futureMapping.getInstrumentToken() + "\","
                + "\"order_type\": \"LIMIT\","
                + "\"transaction_type\": \"" + orderRequestDto.getTransaction_type() + "\","
                + "\"disclosed_quantity\": 0,"
                + "\"trigger_price\": " + orderRequestDto.getEntryPrice()+ ","
                + "\"is_amo\": false"
                + "}";

        System.out.println("Request Body wala : " + requestBody);

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
        System.out.println("Response Code: " + receiveNewOrderResponse.statusCode());
        System.out.println("Response Body: " + receiveNewOrderResponse.body());
        ObjectMapper objectMapper = new ObjectMapper();
        OrderResponse orderResponse = objectMapper.readValue(receiveNewOrderResponse.body(), OrderResponse.class);
        OrderMapper orderMapper = OrderMapper.builder().orderId(orderResponse.getData().getOrderId()).build();
        return orderMapperRepository.save(orderMapper).toString();
    }

    public void cancelAllPreviousOrder(String token) throws IOException, InterruptedException, UnirestException, UpstoxException {
        Iterable<OrderMapper> orderMapperIterable = orderMapperRepository.findAll();
        List<OrderMapper> orderMappers = convertIterableToListOrderMapper(orderMapperIterable);
        if (orderMappers.isEmpty()) {
            log.info("There is no order available in our DB");
            return;
        }

        for (OrderMapper orderMapper : orderMappers)
        {
            //get order status first then cancel
            String orderDetailsUrl = environment.getProperty("upstox_url") + environment.getProperty("order_details") + orderMapper.getOrderId();
            Unirest.setTimeouts(0, 0);
            com.mashape.unirest.http.HttpResponse<String> orderDetailsResponse = Unirest.get(orderDetailsUrl)
                    .header("Accept", "application/json")
                    .header("Authorization", token)
                    .asString();
            ObjectMapper objectMapper = new ObjectMapper();
            OrderData orderData = objectMapper.readValue(orderDetailsResponse.getBody(), OrderData.class);

            if (!orderData.getStatus().equalsIgnoreCase("success")) {
                throw new UpstoxException("There is some error in getting order details");
            }

            if (!orderData.getData().getOrderStatus().equalsIgnoreCase("complete")) {
                String cancelOrderUrl = environment.getProperty("upstox_url") + environment.getProperty("cancel_order") + orderMapper.getOrderId();
                Unirest.setTimeouts(0, 0);
                com.mashape.unirest.http.HttpResponse<String> orderCancelResponse = Unirest.delete(cancelOrderUrl)
                        .header("Accept", "application/json")
                        .header("Authorization", token)
                        .asString();

                OrderResponse orderResponse = objectMapper.readValue(orderCancelResponse.getBody(), OrderResponse.class);

                if (orderResponse.getStatus().equalsIgnoreCase("success")) {
                    orderMapperRepository.deleteById(orderMapper.getId());
                } else {
                    orderMapperRepository.deleteById(orderMapper.getId());
                    log.error("There is some error to cancel order can you please check manually!!");
                }

                System.out.println("Response Code: " + orderCancelResponse.getStatus());
                System.out.println("Response Body: " + orderCancelResponse.getBody());
            }

        }
    }

    public FutureMapping getFutureMapping(OrderRequestDto orderRequestDto) throws UpstoxException {
        Optional<FutureMapping> optionalFutureMappingSymbolName = futureMappingRepository.findBySymbolName(orderRequestDto.getInstrument_name());
        return optionalFutureMappingSymbolName.orElseThrow(() -> new UpstoxException("The provided Symbol is not available in future mappping Database" + orderRequestDto.getInstrument_name()));
    }

    public GetPositionResponseDto getAllPostionCall(String token) throws UnirestException, JsonProcessingException {
        Unirest.setTimeouts(0, 0);
        com.mashape.unirest.http.HttpResponse<String> getAllPositionResponse = Unirest.get(environment.getProperty("upstox_url") + environment.getProperty("get_position"))
                .header("Accept", "application/json")
                .header("Authorization", token)
                .asString();

        System.out.println("Kya mila be : " + getAllPositionResponse);
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(getAllPositionResponse.getBody(), GetPositionResponseDto.class);
    }


    public UpstoxLogin getLoginDetails() throws UpstoxException {
        Optional<UpstoxLogin> optionalUpstoxLogin = upstoxLoginRepository.findByEmail(environment.getProperty("email_id"));
        if (optionalUpstoxLogin.isEmpty()) {
            throw new UpstoxException("Currently we don't have any user information who has logged in!!");
        }
        return optionalUpstoxLogin.get();
    }


   public  OrderRequestDto processBuyOrderRequestData(String requestData) throws JsonProcessingException {
       ObjectMapper objectMapper = new ObjectMapper();
       JsonNode jsonNode = objectMapper.readTree(requestData);
        log.info("Buy Order Request process has started");
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
       System.out.println("Main part : " + Arrays.toString(parts));
       for (String part : parts) {
           System.out.println("Parts : " + part);
           String[] subParts = part.split(":");
           map.put(subParts[0], subParts[1]);
       }

       System.out.println("MAp Data : " + map.get("TYPE"));
       return OrderRequestDto.builder()
               .quantity(quantity)
               .price(Double.parseDouble(map.get("entryPrice")))
               .instrument_name(instrumentName)
               .order_type("LIMIT")
               .transaction_type(map.get("TYPE").trim().equals("LE") ? "BUY" : "SELL")
               .entryPrice(Double.parseDouble(map.get("entryPrice")))
               .build();
   }

    public static List<OrderMapper> convertIterableToListOrderMapper(Iterable<OrderMapper> iterable) {
        List<OrderMapper> list = new ArrayList<>();

        for (OrderMapper item : iterable) {
            list.add(item);
        }

        return list;
    }

    public static List<NextFutureMapping> convertIterableToListNextFutureMapper(Iterable<NextFutureMapping> iterable) {
        List<NextFutureMapping> list = new ArrayList<>();

        for (NextFutureMapping item : iterable) {
            list.add(item);
        }

        return list;
    }

    public static List<FutureMapping> convertIterableToListFutureMapper(Iterable<FutureMapping> iterable) {
        List<FutureMapping> list = new ArrayList<>();

        for (FutureMapping item : iterable) {
            list.add(item);
        }

        return list;
    }

}
