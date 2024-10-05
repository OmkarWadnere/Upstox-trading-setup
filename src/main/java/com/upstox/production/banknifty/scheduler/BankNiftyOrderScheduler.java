package com.upstox.production.banknifty.scheduler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.upstox.production.banknifty.entity.BankNiftyNextOptionMapping;
import com.upstox.production.banknifty.entity.BankNiftyOptionMapping;
import com.upstox.production.banknifty.entity.BankNiftyOrderMapper;
import com.upstox.production.banknifty.repository.BankNiftyNextOptionMapperRepository;
import com.upstox.production.banknifty.repository.BankNiftyOptionMappingRepository;
import com.upstox.production.banknifty.repository.BankNiftyOrderMapperRepository;
import com.upstox.production.banknifty.service.helper.BankNiftyOrderHelper;
import com.upstox.production.centralconfiguration.dto.*;
import com.upstox.production.centralconfiguration.excpetion.UpstoxException;
import com.upstox.production.centralconfiguration.repository.UpstoxLoginRepository;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static com.upstox.production.banknifty.utility.BankNiftyUtility.*;
import static com.upstox.production.centralconfiguration.utility.CentralUtility.tradeSwitch;

@Component
@Slf4j
public class BankNiftyOrderScheduler {

    @Autowired
    private BankNiftyOrderMapperRepository bankNiftyOrderMapperRepository;
    @Autowired
    private Environment environment;
    @Autowired
    private UpstoxLoginRepository upstoxLoginRepository;
    @Autowired
    private BankNiftyOrderHelper bankNiftyOrderHelper;
    @Autowired
    private BankNiftyNextOptionMapperRepository bankNiftyNextOptionMapperRepository;
    @Autowired
    private BankNiftyOptionMappingRepository bankNiftyOptionMappingRepository;

    // reset the token only and clear order db
    @Scheduled(cron = "0 0 7 * * MON-FRI")
    public void everyDay7AmActivity() {
        schedulerToken = "";
        tradeSwitch = true;
        bankNiftyOrderMapperRepository.deleteAll();
        bankNiftyMorningTradeCounter = 0;
    }

    @Scheduled(cron = "0 25 15 * * MON-FRI")
    public void autoSquareOffAtDayEnd() throws IOException, InterruptedException, UnirestException, UpstoxException {
        //cancel all open orders
        bankNiftyOrderHelper.cancelAllOpenOrders();
        bankNiftyOrderHelper.squareOffAllPositions();
        bankNiftyOrderMapperRepository.deleteAll();
    }

    @Scheduled(cron = "0 0 6 * * MON-FRI")
    public void autoExpiryCarryForward() throws IOException, InterruptedException, UnirestException, UpstoxException {
        //cancel all open orders
        Iterable<BankNiftyOptionMapping> bankNiftyOptionMappingIterable = bankNiftyOptionMappingRepository.findAll();
        List<BankNiftyOptionMapping> bankNiftyOptionMappings = convertIterableToListOptionMapping(bankNiftyOptionMappingIterable);
        Iterable<BankNiftyNextOptionMapping> bankNiftyNextOptionMappingIterable = bankNiftyNextOptionMapperRepository.findAll();
        List<BankNiftyNextOptionMapping> bankNiftyNextOptionMappings = convertIterableToListNextOptionMapping(bankNiftyNextOptionMappingIterable);
        BankNiftyNextOptionMapping bankNiftyNextOptionMapping = bankNiftyNextOptionMappings.stream().findFirst().orElse(null);
        BankNiftyOptionMapping bankNiftyOptionMapping = null;
        if (bankNiftyNextOptionMapping != null) {
             bankNiftyOptionMapping = BankNiftyOptionMapping.builder()
                    .numberOfLots(bankNiftyNextOptionMapping.getNumberOfLots())
                    .profitPoints(bankNiftyNextOptionMapping.getProfitPoints())
                    .averagingTimes(bankNiftyNextOptionMapping.getAveragingTimes())
                    .averagingPointInterval(bankNiftyNextOptionMapping.getAveragingPointInterval())
                    .expiryDate(bankNiftyNextOptionMapping.getExpiryDate())
                    .instrumentToken(bankNiftyNextOptionMapping.getInstrumentToken())
                    .quantity(bankNiftyNextOptionMapping.getQuantity())
                    .symbolName(bankNiftyNextOptionMapping.getSymbolName()).build();
        }
        for (BankNiftyOptionMapping bankNiftyOption: bankNiftyOptionMappings) {
            if (LocalDate.now().plusDays(1).equals(bankNiftyOption.getExpiryDate().plusDays(1)) && bankNiftyOptionMapping != null) {
                bankNiftyOptionMappingRepository.deleteAll();
                bankNiftyNextOptionMapperRepository.deleteAll();
                bankNiftyOptionMappingRepository.save(bankNiftyOptionMapping);
            }
        }
    }

    @Scheduled(cron = "*/5 * 9-15 * * MON-FRI")
    public void recurrenceOrderExecutionAndChecks() throws UpstoxException, IOException, InterruptedException, UnirestException {
        if (tradeSwitch) {
            LocalTime now = LocalTime.now();
            List<OrderData> completedOrderList = new ArrayList<>();
            // Check if the current time is between 9:15 AM and 3:30 PM
            if (now.isAfter(LocalTime.of(9, 15)) && now.isBefore(LocalTime.of(15, 25)) && !isBankNiftyMainExecutionRunning) {
                Iterable<BankNiftyOrderMapper> bankNiftyOrderMapperIterable = bankNiftyOrderMapperRepository.findAll();
                List<BankNiftyOrderMapper> bankNiftyOrderMappers = convertIterableToListOrderMapper(bankNiftyOrderMapperIterable);
//            AllOrderDetailsDto allOrderDetailsDto = bankNiftyOrderHelper.getAllOrderDetails(schedulerToken);
                List<BankNiftyOrderMapper> bankNiftyOrderMapperList = bankNiftyOrderMappers.stream()
                        .parallel()
                        .filter(bankNiftyOrderMapper1 -> bankNiftyOrderMapper1.getOrderType().equalsIgnoreCase("SELL"))
                        .toList();
                for (BankNiftyOrderMapper bankNiftyOrderMapper : bankNiftyOrderMapperList) {
                    String orderDetailsUrl = environment.getProperty("upstox_url") + environment.getProperty("order_details") + bankNiftyOrderMapper.getOrderId();
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(orderDetailsUrl))
                            .header("Content-Type", "application/json")
                            .header("Accept", "application/json")
                            .header("Authorization", schedulerToken)
                            .build();
                    HttpClient httpClient = HttpClient.newBuilder().build();
                    ObjectMapper objectMapper = new ObjectMapper();
                    HttpResponse<String> orderDetailsResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                    JsonNode jsonNodeOrderDetails = objectMapper.readTree(orderDetailsResponse.body());
                    String statusOrderDetails = jsonNodeOrderDetails.get("status").asText();
                    if (statusOrderDetails.equalsIgnoreCase("error")) {
                        log.info("There is some error in placing SELL order : " + bankNiftyOrderMapper.getOrderId());
                        continue;
                    }
                    OrderData placedMarketOrderResponse = objectMapper.readValue(orderDetailsResponse.body(), OrderData.class);
                    if (placedMarketOrderResponse.getData().getOrderStatus().equalsIgnoreCase("complete")) {
                        cancelAllBuyOrders(bankNiftyOrderMappers, schedulerToken);
//                        bankNiftyOrderHelper.cancelAllOpenOrders();
                        log.info("Order completed : " + bankNiftyOrderMapper.getOrderId());
                    }
                }
                bankNiftyOrderMapperIterable = bankNiftyOrderMapperRepository.findAll();
                bankNiftyOrderMappers = convertIterableToListOrderMapper(bankNiftyOrderMapperIterable);
                for (BankNiftyOrderMapper bankNiftyOrderMapper : bankNiftyOrderMappers) {
                    if (bankNiftyOrderMapper.getOrderType().equalsIgnoreCase("BUY")) {
                        String orderDetailsUrl = environment.getProperty("upstox_url") + environment.getProperty("order_details") + bankNiftyOrderMapper.getOrderId();
                        HttpRequest request = HttpRequest.newBuilder()
                                .uri(URI.create(orderDetailsUrl))
                                .header("Content-Type", "application/json")
                                .header("Accept", "application/json")
                                .header("Authorization", schedulerToken)
                                .build();
                        HttpClient httpClient = HttpClient.newBuilder().build();
                        ObjectMapper objectMapper = new ObjectMapper();
                        HttpResponse<String> orderDetailsResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                        JsonNode jsonNodeOrderDetails = objectMapper.readTree(orderDetailsResponse.body());
                        String statusOrderDetails = jsonNodeOrderDetails.get("status").asText();
                        if (statusOrderDetails.equalsIgnoreCase("error")) {
                            log.info("There is some error BUY order : " + bankNiftyOrderMapper.getOrderId());
                            continue;
                        }
                        OrderData averageOrderResponse = objectMapper.readValue(orderDetailsResponse.body(), OrderData.class);
                        if (averageOrderResponse.getData().getOrderStatus().equalsIgnoreCase("complete")) {
                            completedOrderList.add(averageOrderResponse);
                        }
                    }
                }
                if (!completedOrderList.isEmpty()) {
                    placeModifyOrder(bankNiftyOrderMappers, completedOrderList, schedulerToken);
                }
            }
        }
    }

    private void cancelAllBuyOrders(List<BankNiftyOrderMapper> bankNiftyOrderMappers, String token) throws UnirestException, IOException, InterruptedException {

        for (BankNiftyOrderMapper bankNiftyOrderMapper : bankNiftyOrderMappers) {
            if (bankNiftyOrderMapper.getOrderType().equalsIgnoreCase("BUY")) {
                String url = "https://api-hft.upstox.com/v2/order/cancel?order_id=" + bankNiftyOrderMapper.getOrderId();

                String acceptHeader = "application/json";

                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("Accept", acceptHeader)
                        .header("Authorization", token)
                        .DELETE()
                        .build();

                HttpResponse<String> cancelOrderResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNodeOrderDetails = objectMapper.readTree(cancelOrderResponse.body());
                String statusOrderDetails = jsonNodeOrderDetails.get("status").asText();
                if (statusOrderDetails.equalsIgnoreCase("error")) {
                    log.info("There is some error in placing cancel order : " + bankNiftyOrderMapper.getOrderId());
                } else {
                    bankNiftyOrderMapperRepository.deleteById(bankNiftyOrderMapper.getId());
                }
            }
        }
        bankNiftyOrderMapperRepository.deleteAll();
    }

    private void placeModifyOrder(List<BankNiftyOrderMapper> bankNiftyOrderMapperList, List<OrderData> completedOrderList, String token) throws UnirestException {
        double total = 0.00;
        int count = completedOrderList.size();
        for (OrderData orderData : completedOrderList) {
            total += orderData.getData().getAveragePrice();
        }
        Iterable<BankNiftyOptionMapping> bankNiftyOptionMappingIterable = bankNiftyOptionMappingRepository.findAll();
        BankNiftyOptionMapping bankNiftyOptionMapping = convertIterableToListOptionMapping(bankNiftyOptionMappingIterable).stream().findFirst().orElse(null);
        if (count != 0 && total != 0.00 && bankNiftyOptionMapping != null) {
            for (BankNiftyOrderMapper bankNiftyOrderMapper : bankNiftyOrderMapperList) {
                if (bankNiftyOrderMapper.getOrderType().equalsIgnoreCase("SELL")) {
                    double averagePrice = total / count;
                    Unirest.setTimeouts(0, 0);
                    JSONObject requestBody = new JSONObject();
                    requestBody.put("quantity", completedOrderList.size() * bankNiftyOptionMapping.getQuantity());
                    requestBody.put("validity", "DAY");
                    requestBody.put("price", averagePrice);
                    requestBody.put("order_id", bankNiftyOrderMapper.getOrderId());
                    requestBody.put("order_type", "LIMIT");
                    requestBody.put("disclosed_quantity", 0);
                    requestBody.put("trigger_price", 0);

                    com.mashape.unirest.http.HttpResponse<String> response = Unirest.put("https://api-hft.upstox.com/v2/order/modify")
                            .header("Accept", "application/json")
                            .header("Authorization", token)  // Make sure token is properly defined
                            .header("Content-Type", "application/json")
                            .body(requestBody.toString())  // Pass the JSON payload as string
                            .asString();
                }
            }
        }
    }


    private static List<BankNiftyOptionMapping> convertIterableToListOptionMapping(Iterable<BankNiftyOptionMapping> iterable) {
        List<BankNiftyOptionMapping> list = new ArrayList<>();

        for (BankNiftyOptionMapping item : iterable) {
            list.add(item);
        }

        return list;
    }

    private static List<BankNiftyNextOptionMapping> convertIterableToListNextOptionMapping(Iterable<BankNiftyNextOptionMapping> iterable) {
        List<BankNiftyNextOptionMapping> list = new ArrayList<>();

        for (BankNiftyNextOptionMapping item : iterable) {
            list.add(item);
        }

        return list;
    }


    private static List<BankNiftyOrderMapper> convertIterableToListOrderMapper(Iterable<BankNiftyOrderMapper> iterable) {
        List<BankNiftyOrderMapper> list = new ArrayList<>();

        for (BankNiftyOrderMapper item : iterable) {
            list.add(item);
        }

        return list;
    }
}
