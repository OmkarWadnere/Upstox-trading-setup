package com.upstox.production.nifty.scheduler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.upstox.production.centralconfiguration.dto.GetPositionDataDto;
import com.upstox.production.centralconfiguration.dto.GetPositionResponseDto;
import com.upstox.production.centralconfiguration.dto.OrderData;
import com.upstox.production.centralconfiguration.excpetion.UpstoxException;
import com.upstox.production.centralconfiguration.repository.TradeAccessUpstoxLoginRepository;
import com.upstox.production.nifty.dto.NiftyLtpResponseDTO;
import com.upstox.production.nifty.dto.NiftyOptionChainResponseDTO;
import com.upstox.production.nifty.dto.NiftyOptionDTO;
import com.upstox.production.nifty.entity.NiftyNextOptionMapping;
import com.upstox.production.nifty.entity.NiftyOptionMapping;
import com.upstox.production.nifty.entity.NiftyOrderMapper;
import com.upstox.production.nifty.repository.NiftyNextOptionMapperRepository;
import com.upstox.production.nifty.repository.NiftyOptionMappingRepository;
import com.upstox.production.nifty.repository.NiftyOrderMapperRepository;
import com.upstox.production.nifty.service.helper.NiftyOrderHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.upstox.production.centralconfiguration.utility.CentralUtility.atulSchedulerToken;
import static com.upstox.production.centralconfiguration.utility.CentralUtility.omkarSchedulerToken;
import static com.upstox.production.centralconfiguration.utility.CentralUtility.tradeSwitch;
import static com.upstox.production.nifty.enums.OrderType.BUY_ENTRY;
import static com.upstox.production.nifty.enums.OrderType.SELL_ENTRY;
import static com.upstox.production.nifty.utility.NiftyUtility.currentParentInstrument;
import static com.upstox.production.nifty.utility.NiftyUtility.currentTradeExpiryDate;
import static com.upstox.production.nifty.utility.NiftyUtility.currentTradeType;
import static com.upstox.production.nifty.utility.NiftyUtility.isNiftyMainExecutionRunning;
import static com.upstox.production.nifty.utility.NiftyUtility.niftyCurrentInstrumentToken;
import static com.upstox.production.nifty.utility.NiftyUtility.niftyOptionBuyPrice;
import static com.upstox.production.nifty.utility.NiftyUtility.niftySlPrice;

@Component
@Slf4j
public class NiftyOrderScheduler {

    @Autowired
    private NiftyOrderMapperRepository niftyOrderMapperRepository;
    @Autowired
    private Environment environment;
    @Autowired
    private TradeAccessUpstoxLoginRepository tradeAccessUpstoxLoginRepository;
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
        omkarSchedulerToken = "";
        niftyOrderMapperRepository.deleteAll();
    }

    @Scheduled(cron = "0 30 8 * * MON-FRI")
    public void autoExpirySwitching() throws UpstoxException {
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
        for (NiftyOptionMapping niftyOption : niftyOptionMappings) {
            if (LocalDate.now().equals(niftyOption.getExpiryDate().minusDays(1)) && niftyOptionMapping != null) {
                niftyOptionMappingRepository.deleteAll();
                niftyNextOptionMapperRepository.deleteAll();
                niftyOptionMappingRepository.save(niftyOptionMapping);
            } else {
                throw new UpstoxException("There is some issue in switching in expiry mapping");
            }
        }
    }

    @Scheduled(cron = "0 30 13 * * MON-FRI")
    public void switchPositionToNextExpiry() throws UpstoxException, UnirestException, IOException, InterruptedException, URISyntaxException {
        Optional<NiftyOptionMapping> niftyOptionMappingOptional = niftyOptionMappingRepository.findByInstrumentToken(currentParentInstrument);
        if (niftyOptionMappingOptional.isEmpty()) {
            throw new UpstoxException("There is no option mapping present");
        }
        NiftyOptionMapping niftyOptionMapping = niftyOptionMappingOptional.get();
        ObjectMapper objectMapper = new ObjectMapper();
        if (currentTradeExpiryDate.minusDays(1).equals(LocalDate.now())) {
            niftyCurrentInstrumentToken = niftyOptionMapping.getInstrumentToken();
            if (currentTradeType.equals(BUY_ENTRY.getOrderType())) {
                GetPositionResponseDto getPositionResponseDto = niftyOrderHelper.getAllPositionCall(atulSchedulerToken);
                if (!getPositionResponseDto.getStatus().equalsIgnoreCase("success")) {
                    throw new UpstoxException("There is some issue in getting current position details");
                }
                List<GetPositionDataDto> getPositionDataDtos = getPositionResponseDto.getData();
                for (GetPositionDataDto getPositionDataDto : getPositionDataDtos) {
                    niftyOrderHelper.cancelAllOpenOrders();
                    if (getPositionDataDto.getQuantity() > 0) {
                        String requestBody = "{"
                                + "\"quantity\": " + getPositionDataDto.getQuantity() + ","
                                + "\"product\": \"D\","
                                + "\"validity\": \"DAY\","
                                + "\"price\": 0,"
                                + "\"tag\": \"string\","
                                + "\"instrument_token\": \"" + getPositionDataDto.getInstrumentToken() + "\","
                                + "\"order_type\": \"MARKET\","
                                + "\"transaction_type\": \"SELL\","
                                + "\"disclosed_quantity\": 0,"
                                + "\"trigger_price\": 0,"
                                + "\"is_amo\": false"
                                + "}";
                        log.info("Request data :  " + requestBody);
                        HttpClient httpClient = HttpClient.newHttpClient();

                        String orderUrl = environment.getProperty("upstox_url") + environment.getProperty("place_order");
                        HttpRequest request = HttpRequest.newBuilder()
                                .uri(URI.create(orderUrl))
                                .header("Content-Type", "application/json")
                                .header("Accept", "application/json")
                                .header("Authorization", atulSchedulerToken)
                                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                                .build();
                        HttpResponse<String> placeOptionBuyOrderResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                        JsonNode jsonNodeOrderDetails = objectMapper.readTree(placeOptionBuyOrderResponse.body());
                        log.info("Placed order response " + placeOptionBuyOrderResponse.body());
                        String statusOrderDetails = jsonNodeOrderDetails.get("status").asText();
                        if (statusOrderDetails.equalsIgnoreCase("error")) {
                            throw new UpstoxException("There is some error in placing square off order");
                        }
                        OrderData orderData = objectMapper.readValue(placeOptionBuyOrderResponse.body(), OrderData.class);
                        log.info("Order placed data : " + orderData);

                        // get average price of market order
                        String orderDetailsUrl = environment.getProperty("upstox_url") + environment.getProperty("order_details") + orderData.getData().getOrderId();
                        request = HttpRequest.newBuilder()
                                .uri(URI.create(orderDetailsUrl))
                                .header("Content-Type", "application/json")
                                .header("Accept", "application/json")
                                .header("Authorization", atulSchedulerToken)
                                .build();
                        HttpResponse<String> orderDetailsResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                        OrderData placedMarketOrderResponse = objectMapper.readValue(orderDetailsResponse.body(), OrderData.class);
                        log.info("Placed square off current expiry trade market order response : " + placedMarketOrderResponse);
                        double tradePoints = placedMarketOrderResponse.getData().getAveragePrice() - niftyOptionBuyPrice;
                        NiftyOptionChainResponseDTO niftyOptionChainResponseDTO = niftyOrderHelper.getOptionChain(niftyOptionMapping, atulSchedulerToken);
                        log.info("Option chain details : " + niftyOptionChainResponseDTO);
                        NiftyOptionDTO niftyOptionDTO = niftyOrderHelper.filterNiftyOptionStrike(niftyOptionChainResponseDTO, "CALL_BUY");
                        if (niftyOptionDTO == null) {
                            isNiftyMainExecutionRunning = false;
                            log.error("There is no option in our range please check option range received for reference : " + niftyOptionChainResponseDTO);
                            throw new UpstoxException("There is no option in our range");
                        }
                        log.info("Selected strike : " + niftyOptionDTO);
                        niftyOrderHelper.placeBuyOrder(niftyOptionDTO, niftyOptionMapping, atulSchedulerToken);
                        if (tradePoints >= 0.00) {
                            // 400 - 100 - 65
                            niftySlPrice = niftyOptionBuyPrice - tradePoints - niftyOptionMapping.getStopLossPriceRange();
                        } else {
                            // 400 - (65 + -50)
                            niftySlPrice = niftyOptionBuyPrice - (niftyOptionMapping.getStopLossPriceRange() + tradePoints);
                        }
                    }
                }

            } else if (currentTradeType.equals(SELL_ENTRY.getOrderType())) {
                GetPositionResponseDto getPositionResponseDto = niftyOrderHelper.getAllPositionCall(atulSchedulerToken);
                if (!getPositionResponseDto.getStatus().equalsIgnoreCase("success")) {
                    throw new UpstoxException("There is some issue in getting current position details");
                }
                List<GetPositionDataDto> getPositionDataDtos = getPositionResponseDto.getData();
                for (GetPositionDataDto getPositionDataDto : getPositionDataDtos) {
                    niftyOrderHelper.cancelAllOpenOrders();
                    if (getPositionDataDto.getQuantity() > 0) {
                        String requestBody = "{"
                                + "\"quantity\": " + getPositionDataDto.getQuantity() + ","
                                + "\"product\": \"D\","
                                + "\"validity\": \"DAY\","
                                + "\"price\": 0,"
                                + "\"tag\": \"string\","
                                + "\"instrument_token\": \"" + getPositionDataDto.getInstrumentToken() + "\","
                                + "\"order_type\": \"MARKET\","
                                + "\"transaction_type\": \"SELL\","
                                + "\"disclosed_quantity\": 0,"
                                + "\"trigger_price\": 0,"
                                + "\"is_amo\": false"
                                + "}";
                        log.info("Request data :  " + requestBody);
                        HttpClient httpClient = HttpClient.newHttpClient();

                        String orderUrl = environment.getProperty("upstox_url") + environment.getProperty("place_order");
                        HttpRequest request = HttpRequest.newBuilder()
                                .uri(URI.create(orderUrl))
                                .header("Content-Type", "application/json")
                                .header("Accept", "application/json")
                                .header("Authorization", atulSchedulerToken)
                                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                                .build();
                        HttpResponse<String> placeOptionBuyOrderResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                        JsonNode jsonNodeOrderDetails = objectMapper.readTree(placeOptionBuyOrderResponse.body());
                        log.info("Placed order response " + placeOptionBuyOrderResponse.body());
                        String statusOrderDetails = jsonNodeOrderDetails.get("status").asText();
                        if (statusOrderDetails.equalsIgnoreCase("error")) {
                            throw new UpstoxException("There is some error in placing square off order");
                        }
                        OrderData orderData = objectMapper.readValue(placeOptionBuyOrderResponse.body(), OrderData.class);
                        log.info("Order placed data : " + orderData);

                        // get average price of market order
                        String orderDetailsUrl = environment.getProperty("upstox_url") + environment.getProperty("order_details") + orderData.getData().getOrderId();
                        request = HttpRequest.newBuilder()
                                .uri(URI.create(orderDetailsUrl))
                                .header("Content-Type", "application/json")
                                .header("Accept", "application/json")
                                .header("Authorization", atulSchedulerToken)
                                .build();
                        HttpResponse<String> orderDetailsResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                        OrderData placedMarketOrderResponse = objectMapper.readValue(orderDetailsResponse.body(), OrderData.class);
                        log.info("Placed square off current expiry trade market order response : " + placedMarketOrderResponse);
                        double tradePoints = placedMarketOrderResponse.getData().getAveragePrice() - niftyOptionBuyPrice;
                        NiftyOptionChainResponseDTO niftyOptionChainResponseDTO = niftyOrderHelper.getOptionChain(niftyOptionMapping, atulSchedulerToken);
                        log.info("Option chain details : " + niftyOptionChainResponseDTO);
                        NiftyOptionDTO niftyOptionDTO = niftyOrderHelper.filterNiftyOptionStrike(niftyOptionChainResponseDTO, "PUT_BUY");
                        if (niftyOptionDTO == null) {
                            isNiftyMainExecutionRunning = false;
                            log.error("There is no option in our range please check option range received for reference : " + niftyOptionChainResponseDTO);
                            throw new UpstoxException("There is no option in our range");
                        }
                        log.info("Selected strike : " + niftyOptionDTO);
                        niftyOrderHelper.placeBuyOrder(niftyOptionDTO, niftyOptionMapping, atulSchedulerToken);
                        if (tradePoints >= 0.00) {
                            // 400 - 100 - 65
                            niftySlPrice = niftyOptionBuyPrice - tradePoints - niftyOptionMapping.getStopLossPriceRange();
                        } else {
                            // 400 - (65 + -50)
                            niftySlPrice = niftyOptionBuyPrice - (niftyOptionMapping.getStopLossPriceRange() + tradePoints);
                        }
                    }
                }
            }

        }
    }

    @Scheduled(cron = "*/2 * 9-15 * * MON-FRI")
    public void stoplossExecution() throws UpstoxException, IOException, InterruptedException, UnirestException {
        log.info("Per Two Second execution start : " + LocalDateTime.now());

        if (tradeSwitch) {
            LocalTime now = LocalTime.now();
            List<OrderData> completedOrderList = new ArrayList<>();
            // Check if the current time is between 9:15 AM and 3:30 PM
            if (now.isAfter(LocalTime.of(9, 14)) && now.isBefore(LocalTime.of(15, 31)) && !isNiftyMainExecutionRunning) {
                NiftyLtpResponseDTO niftyLtpResponseDTO = niftyOrderHelper.fetchNiftyOptionCmp();
                double currentOptionLtp = niftyLtpResponseDTO.getData().values().stream().findFirst().get().getLast_price();
                if (currentOptionLtp <= niftySlPrice) {
                    niftyOrderHelper.squareOffAllPositions();
                    niftyOrderHelper.cancelAllOpenOrders();
                }
            }
        }
        log.info("Per Two Second execution end : " + LocalDateTime.now());
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
