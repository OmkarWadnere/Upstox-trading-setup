package com.upstox.production.nifty.scheduler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.upstox.production.centralconfiguration.dto.OrderData;
import com.upstox.production.centralconfiguration.excpetion.UpstoxException;
import com.upstox.production.centralconfiguration.repository.UpstoxLoginRepository;
import com.upstox.production.nifty.dto.NiftyLtpResponseDTO;
import com.upstox.production.nifty.entity.NiftyNextOptionMapping;
import com.upstox.production.nifty.entity.NiftyOptionMapping;
import com.upstox.production.nifty.entity.NiftyOrderMapper;
import com.upstox.production.nifty.repository.NiftyNextOptionMapperRepository;
import com.upstox.production.nifty.repository.NiftyOptionMappingRepository;
import com.upstox.production.nifty.repository.NiftyOrderMapperRepository;
import com.upstox.production.nifty.service.helper.NiftyOrderHelper;
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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static com.upstox.production.centralconfiguration.utility.CentralUtility.*;
import static com.upstox.production.nifty.utility.NiftyUtility.*;

@Component
@Slf4j
public class NiftyOrderScheduler {

    @Autowired
    private NiftyOrderMapperRepository niftyOrderMapperRepository;
    @Autowired
    private Environment environment;
    @Autowired
    private UpstoxLoginRepository upstoxLoginRepository;
    @Autowired
    private NiftyOrderHelper niftyOrderHelper;
    @Autowired
    private NiftyNextOptionMapperRepository niftyNextOptionMapperRepository;
    @Autowired
    private NiftyOptionMappingRepository niftyOptionMappingRepository;

    // reset the token only and clear order db
    @Scheduled(cron = "0 0 7 * * MON-FRI")
    public void everyDay7AmActivity() {
        atulSchedulerToken = "";
        tradeSwitch = true;
        niftyOrderMapperRepository.deleteAll();
        niftyMorningTradeCounter = 0;
    }

    @Scheduled(cron = "0 25 15 * * MON-FRI")
    public void autoSquareOffAtDayEnd() throws IOException, InterruptedException, UnirestException, UpstoxException {
        //cancel all open orders
        niftyOrderHelper.cancelAllOpenOrders();
        niftyOrderHelper.squareOffAllPositions();
        niftyOrderMapperRepository.deleteAll();
    }

    @Scheduled(cron = "0 0 6 * * MON-FRI")
    public void autoExpiryCarryForward() throws IOException, InterruptedException, UnirestException, UpstoxException {
        //cancel all open orders
        Iterable<NiftyOptionMapping> niftyOptionMappingIterable = niftyOptionMappingRepository.findAll();
        List<NiftyOptionMapping> niftyOptionMappings = convertIterableToListOptionMapping(niftyOptionMappingIterable);
        Iterable<NiftyNextOptionMapping> niftyNextOptionMappingIterable = niftyNextOptionMapperRepository.findAll();
        List<NiftyNextOptionMapping> niftyNextOptionMappings = convertIterableToListNextOptionMapping(niftyNextOptionMappingIterable);
        NiftyNextOptionMapping niftyNextOptionMapping = niftyNextOptionMappings.stream().findFirst().orElse(null);
        NiftyOptionMapping niftyOptionMapping = null;
        if (niftyNextOptionMapping != null) {
             niftyOptionMapping = NiftyOptionMapping.builder()
                    .numberOfLots(niftyNextOptionMapping.getNumberOfLots())
                    .profitPoints(niftyNextOptionMapping.getProfitPoints())
                    .averagingTimes(niftyNextOptionMapping.getAveragingTimes())
                    .averagingPointInterval(niftyNextOptionMapping.getAveragingPointInterval())
                    .expiryDate(niftyNextOptionMapping.getExpiryDate())
                    .instrumentToken(niftyNextOptionMapping.getInstrumentToken())
                    .quantity(niftyNextOptionMapping.getQuantity())
                    .symbolName(niftyNextOptionMapping.getSymbolName()).build();
        }
        for (NiftyOptionMapping niftyOption: niftyOptionMappings) {
            if (LocalDate.now().equals(niftyOption.getExpiryDate().plusDays(1)) && niftyOptionMapping != null) {
                niftyOptionMappingRepository.deleteAll();
                niftyNextOptionMapperRepository.deleteAll();
                niftyOptionMappingRepository.save(niftyOptionMapping);
            }
        }
    }

    @Scheduled(cron = "*/2 * 9-15 * * MON-FRI")
    public void getNiftyOptionLtp() throws UpstoxException, IOException, InterruptedException, UnirestException {
        log.info("Per Two Second execution start : " + LocalDateTime.now());
        HttpClient httpClient = HttpClient.newHttpClient();
        ObjectMapper objectMapper = new ObjectMapper();
        if (tradeSwitch) {
            LocalTime now = LocalTime.now();
            List<OrderData> completedOrderList = new ArrayList<>();
            // Check if the current time is between 9:15 AM and 3:30 PM
            if (now.isAfter(LocalTime.of(9, 15)) && now.isBefore(LocalTime.of(15, 25)) && !isNiftyMainExecutionRunning) {
                NiftyLtpResponseDTO niftyLtpResponseDTO = fetchNiftyOptionCmp();
                double currentOptionLtp = niftyLtpResponseDTO.getData().values().stream().findFirst().get().getLast_price();
                if (currentOptionLtp <= niftyTrailSlPrice) {
                    niftyOrderHelper.squareOffAllPositions();
                    niftyOrderHelper.cancelAllOpenOrders();
                }
                Iterable<NiftyOrderMapper> niftyOrderMapperIterable = niftyOrderMapperRepository.findAll();
                List<NiftyOrderMapper> niftyOrderMappers = convertIterableToListOrderMapper(niftyOrderMapperIterable);
                for (NiftyOrderMapper niftyOrderMapper : niftyOrderMappers) {
                    if (niftyOrderMapper.getOrderType().equalsIgnoreCase("SELL")) {
                        String orderDetailsUrl = environment.getProperty("upstox_url") + environment.getProperty("order_details") + niftyOrderMapper.getOrderId();
                        HttpRequest request = HttpRequest.newBuilder()
                                .uri(URI.create(orderDetailsUrl))
                                .header("Content-Type", "application/json")
                                .header("Accept", "application/json")
                                .header("Authorization", atulSchedulerToken)
                                .build();
                        HttpResponse<String> orderDetailsResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                        OrderData targetOrderResponse = objectMapper.readValue(orderDetailsResponse.body(), OrderData.class);
                        if (targetOrderResponse.getData().getOrderStatus().equalsIgnoreCase("complete")) {
                            niftyTrailSlPrice = niftyOptionBuyPrice;
                            niftyOptionHighPrice = niftyOptionInitialTargetPrice;
                            niftyOrderMapperRepository.delete(niftyOrderMapper);
                        }
                    }
                }
                niftyOptionHighPrice = Math.max(niftyOptionHighPrice, currentOptionLtp);
                niftyTrailSlPrice = niftyOptionHighPrice-50 < 0 ? ((niftyOptionHighPrice-50) * (-1)) : niftyOptionHighPrice-50;
            }
        }
        log.info("Per Two Second execution end : " + LocalDateTime.now());
    }


//    @Scheduled(cron = "*/5 * 9-15 * * MON-FRI")
    public void recurrenceOrderExecutionAndChecks() throws UpstoxException, IOException, InterruptedException, UnirestException {
        log.info("Trade switch : " + tradeSwitch + "at time " + LocalDateTime.now());
        if (tradeSwitch) {
            LocalTime now = LocalTime.now();
            List<OrderData> completedOrderList = new ArrayList<>();
            // Check if the current time is between 9:15 AM and 3:30 PM
            if (now.isAfter(LocalTime.of(9, 15)) && now.isBefore(LocalTime.of(15, 25)) && !isNiftyMainExecutionRunning) {
                Iterable<NiftyOrderMapper> niftyOrderMapperIterable = niftyOrderMapperRepository.findAll();
                List<NiftyOrderMapper> niftyOrderMappers = convertIterableToListOrderMapper(niftyOrderMapperIterable);
                log.info("All schedular orders : " + niftyOrderMappers);
//            AllOrderDetailsDto allOrderDetailsDto = niftyOrderHelper.getAllOrderDetails(schedulerToken);
                List<NiftyOrderMapper> niftyOrderMapperList = niftyOrderMappers.stream()
                        .parallel()
                        .filter(niftyOrderMapper1 -> niftyOrderMapper1.getOrderType().equalsIgnoreCase("SELL"))
                        .toList();
                log.info("All schedular orders SELL: " + niftyOrderMapperList);
                for (NiftyOrderMapper niftyOrderMapper : niftyOrderMapperList) {
                    String orderDetailsUrl = environment.getProperty("upstox_url") + environment.getProperty("order_details") + niftyOrderMapper.getOrderId();
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(orderDetailsUrl))
                            .header("Content-Type", "application/json")
                            .header("Accept", "application/json")
                            .header("Authorization", atulSchedulerToken)
                            .build();
                    HttpClient httpClient = HttpClient.newBuilder().build();
                    ObjectMapper objectMapper = new ObjectMapper();
                    HttpResponse<String> orderDetailsResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                    JsonNode jsonNodeOrderDetails = objectMapper.readTree(orderDetailsResponse.body());
                    String statusOrderDetails = jsonNodeOrderDetails.get("status").asText();
                    if (statusOrderDetails.equalsIgnoreCase("error")) {
                        log.info("There is some error in placing SELL order : " + niftyOrderMapper.getOrderId());
                        continue;
                    }
                    OrderData placedMarketOrderResponse = objectMapper.readValue(orderDetailsResponse.body(), OrderData.class);
                    log.info("schedular orders SELL Response: " + placedMarketOrderResponse);
                    if (placedMarketOrderResponse.getData().getOrderStatus().equalsIgnoreCase("complete")) {
                         cancelAllBuyOrders(niftyOrderMappers, atulSchedulerToken);
//                       niftyOrderHelper.cancelAllOpenOrders();
                        log.info("Order completed : " + niftyOrderMapper.getOrderId());
                    }
                }
                niftyOrderMapperIterable = niftyOrderMapperRepository.findAll();
                niftyOrderMappers = convertIterableToListOrderMapper(niftyOrderMapperIterable);
                log.info("All schedular orders New: " + niftyOrderMappers);
                for (NiftyOrderMapper niftyOrderMapper : niftyOrderMappers) {
                    if (niftyOrderMapper.getOrderType().equalsIgnoreCase("BUY")) {
                        String orderDetailsUrl = environment.getProperty("upstox_url") + environment.getProperty("order_details") + niftyOrderMapper.getOrderId();
                        HttpRequest request = HttpRequest.newBuilder()
                                .uri(URI.create(orderDetailsUrl))
                                .header("Content-Type", "application/json")
                                .header("Accept", "application/json")
                                .header("Authorization", atulSchedulerToken)
                                .build();
                        HttpClient httpClient = HttpClient.newBuilder().build();
                        ObjectMapper objectMapper = new ObjectMapper();
                        HttpResponse<String> orderDetailsResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                        JsonNode jsonNodeOrderDetails = objectMapper.readTree(orderDetailsResponse.body());
                        String statusOrderDetails = jsonNodeOrderDetails.get("status").asText();
                        if (statusOrderDetails.equalsIgnoreCase("error")) {
                            log.info("There is some error BUY order : " + niftyOrderMapper.getOrderId());
                            continue;
                        }
                        OrderData averageOrderResponse = objectMapper.readValue(orderDetailsResponse.body(), OrderData.class);
                        log.info("Average schedular order: " + averageOrderResponse);
                        if (averageOrderResponse.getData().getOrderStatus().equalsIgnoreCase("complete")) {
                            completedOrderList.add(averageOrderResponse);
                            log.info("Complete order list : " + completedOrderList);
                        }
                    }
                }
                if (!completedOrderList.isEmpty()) {
                    placeModifyOrder(niftyOrderMappers, completedOrderList, atulSchedulerToken);
                }
            }
        }
    }

    private void cancelAllBuyOrders(List<NiftyOrderMapper> niftyOrderMappers, String token) throws UnirestException, IOException, InterruptedException {
        log.info("Cancel all orders details : " + niftyOrderMappers);
        for (NiftyOrderMapper niftyOrderMapper : niftyOrderMappers) {
            if (niftyOrderMapper.getOrderType().equalsIgnoreCase("BUY")) {
                String url = "https://api-hft.upstox.com/v2/order/cancel?order_id=" + niftyOrderMapper.getOrderId();

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
                    log.info("There is some error in placing cancel order : " + niftyOrderMapper.getOrderId());
                } else {
                    niftyOrderMapperRepository.deleteById(niftyOrderMapper.getId());
                }
                log.info("Cancel all orders detail response: " + jsonNodeOrderDetails.asText());
            }
        }
        niftyOrderMapperRepository.deleteAll();
    }

    private void placeModifyOrder(List<NiftyOrderMapper> niftyOrderMapperList, List<OrderData> completedOrderList, String token) throws UnirestException {
        double total = 0.00;
        int count = completedOrderList.size();
        for (OrderData orderData : completedOrderList) {
            total += orderData.getData().getAveragePrice();
        }
        log.info("Total price : " + total);
        log.info("Count : " + count);
        Iterable<NiftyOptionMapping> niftyOptionMappingIterable = niftyOptionMappingRepository.findAll();
        NiftyOptionMapping niftyOptionMapping = convertIterableToListOptionMapping(niftyOptionMappingIterable).stream().findFirst().orElse(null);
        log.info("Place Modify Data : " + niftyOptionMapping);
        if (count != 0 && total != 0.00 && niftyOptionMapping != null) {
            for (NiftyOrderMapper niftyOrderMapper : niftyOrderMapperList) {
                if (niftyOrderMapper.getOrderType().equalsIgnoreCase("SELL")) {
                    double averagePrice = total / count;
                    Unirest.setTimeouts(0, 0);
                    JSONObject requestBody = new JSONObject();
                    requestBody.put("quantity", completedOrderList.size() * niftyOptionMapping.getQuantity());
                    requestBody.put("validity", "DAY");
                    requestBody.put("price", averagePrice);
                    requestBody.put("order_id", niftyOrderMapper.getOrderId());
                    requestBody.put("order_type", "LIMIT");
                    requestBody.put("disclosed_quantity", 0);
                    requestBody.put("trigger_price", 0);

                    com.mashape.unirest.http.HttpResponse<String> response = Unirest.put("https://api-hft.upstox.com/v2/order/modify")
                            .header("Accept", "application/json")
                            .header("Authorization", token)  // Make sure token is properly defined
                            .header("Content-Type", "application/json")
                            .body(requestBody.toString())  // Pass the JSON payload as string
                            .asString();
                    log.info("Recieved Modify Order Response : " + response.getBody());
                }
            }
        }
    }

    public NiftyLtpResponseDTO fetchNiftyOptionCmp() throws IOException, InterruptedException {
        String niftyInstrument = niftyCurrentInstrument.replace("|" , "%7C");
        String url = "https://api.upstox.com/v2/market-quote/ltp?instrument_key=" + niftyInstrument;
        String acceptHeader = "application/json";

        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", acceptHeader)
                .header("Authorization", omkarSchedulerToken)
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        ObjectMapper objectMapper = new ObjectMapper();
        log.info("Current market price received : " + response.body());
        return objectMapper.readValue(response.body(), NiftyLtpResponseDTO.class);
    }

    private static List<NiftyOptionMapping> convertIterableToListOptionMapping(Iterable<NiftyOptionMapping> iterable) {
        List<NiftyOptionMapping> list = new ArrayList<>();

        for (NiftyOptionMapping item : iterable) {
            list.add(item);
        }

        return list;
    }

    private static List<NiftyNextOptionMapping> convertIterableToListNextOptionMapping(Iterable<NiftyNextOptionMapping> iterable) {
        List<NiftyNextOptionMapping> list = new ArrayList<>();

        for (NiftyNextOptionMapping item : iterable) {
            list.add(item);
        }

        return list;
    }


    private static List<NiftyOrderMapper> convertIterableToListOrderMapper(Iterable<NiftyOrderMapper> iterable) {
        List<NiftyOrderMapper> list = new ArrayList<>();

        for (NiftyOrderMapper item : iterable) {
            list.add(item);
        }

        return list;
    }
}
