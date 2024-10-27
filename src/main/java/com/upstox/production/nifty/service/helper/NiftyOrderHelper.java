package com.upstox.production.nifty.service.helper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.upstox.production.centralconfiguration.dto.*;
import com.upstox.production.centralconfiguration.excpetion.UpstoxException;
import com.upstox.production.nifty.dto.NiftyLtpResponseDTO;
import com.upstox.production.nifty.dto.NiftyOptionChainDataDTO;
import com.upstox.production.nifty.dto.NiftyOptionChainResponseDTO;
import com.upstox.production.nifty.dto.NiftyOptionDTO;
import com.upstox.production.nifty.entity.NiftyOptionMapping;
import com.upstox.production.nifty.entity.NiftyOrderMapper;
import com.upstox.production.nifty.repository.NiftyOptionMappingRepository;
import com.upstox.production.nifty.repository.NiftyOrderMapperRepository;
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

import static com.upstox.production.centralconfiguration.utility.CentralUtility.atulSchedulerToken;
import static com.upstox.production.nifty.utility.NiftyUtility.*;


@Component
@Slf4j
public class NiftyOrderHelper {

    @Autowired
    private NiftyOptionMappingRepository niftyOptionMappingRepository;
    @Autowired
    private Environment environment;
    @Autowired
    private NiftyOrderMapperRepository niftyOrderMapperRepository;

    public NiftyOptionChainResponseDTO getOptionChain(NiftyOptionMapping niftyOptionMapping, String token) throws UpstoxException, URISyntaxException, IOException, InterruptedException {
        log.info("Get option chain details: " +niftyOptionMapping);
        HttpClient httpClient = HttpClient.newBuilder().build();
        String nifty = "NSE_INDEX%7CNifty%2050";
        String expiryDate = niftyOptionMapping.getExpiryDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        log.info(" Nifty and expiry date: " + nifty + " " + expiryDate);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("https://api.upstox.com/v2/option/chain?instrument_key=" + nifty + "&expiry_date=" + expiryDate))
                .header("Accept", "application/json")
                .header("Authorization", token)
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        ObjectMapper objectMapper = new ObjectMapper();
        log.info("Option chain : " + response.body());
        return objectMapper.readValue(response.body(), NiftyOptionChainResponseDTO.class);
    }

    public NiftyOptionDTO filterNiftyOptionStrike(NiftyOptionChainResponseDTO niftyOptionChainResponseDTO, String transactionType) {
        // Check for Call Buy options
        if (transactionType.equalsIgnoreCase("CALL_BUY")) {
            // First try to get call options with multiples of 100
            Optional<NiftyOptionDTO> callOption = niftyOptionChainResponseDTO.getData().stream()
                    .parallel()
                    .filter(data -> data.getCall_options() != null) // Ensure call options are not null
                    .filter(data -> {
                        NiftyOptionDTO callOptions = data.getCall_options();
                        double ltp = callOptions.getMarket_data().getLtp();
                        return ltp >= 401.00 && ltp <= 480.00 && data.getStrike_price() % 100 == 0; // Check LTP and strike price
                    })
                    .map(NiftyOptionChainDataDTO::getCall_options)
                    .max(Comparator.comparingDouble(o -> o.getMarket_data().getLtp()));

            // If no multiples of 100 found, fall back to multiples of 100 nut in the range of 401 to 500
            if (callOption.isEmpty()) {
                callOption = niftyOptionChainResponseDTO.getData().stream()
                        .parallel()
                        .filter(data -> data.getCall_options() != null) // Ensure call options are not null
                        .filter(data -> {
                            NiftyOptionDTO callOptions = data.getCall_options();
                            double ltp = callOptions.getMarket_data().getLtp();
                            return ltp >= 401.00 && ltp <= 500.00 && data.getStrike_price() % 100 == 0; // Check LTP and strike price
                        })
                        .map(NiftyOptionChainDataDTO::getCall_options)
                        .max(Comparator.comparingDouble(o -> o.getMarket_data().getLtp()));
            }
            return callOption.orElse(null); // Return the found call option or null

            // Check for Put Buy options
        } else if (transactionType.equalsIgnoreCase("PUT_BUY")) {
            // First try to get put options with multiples of 100
            Optional<NiftyOptionDTO> putOption = niftyOptionChainResponseDTO.getData().stream()
                    .parallel()
                    .filter(data -> data.getPut_options() != null) // Ensure put options are not null
                    .filter(data -> {
                        NiftyOptionDTO putOptions = data.getPut_options();
                        double ltp = putOptions.getMarket_data().getLtp();
                        return ltp >= 401.00 && ltp <= 480.00 && data.getStrike_price() % 100 == 0; // Check LTP and strike price
                    })
                    .map(NiftyOptionChainDataDTO::getPut_options)
                    .max(Comparator.comparingDouble(o -> o.getMarket_data().getLtp()));

            // If no multiples of 100 found, fall back to multiples of 100 but range is 401 to 500
            if (putOption.isEmpty()) {
                putOption = niftyOptionChainResponseDTO.getData().stream()
                        .parallel()
                        .filter(data -> data.getPut_options() != null) // Ensure put options are not null
                        .filter(data -> {
                            NiftyOptionDTO putOptions = data.getPut_options();
                            double ltp = putOptions.getMarket_data().getLtp();
                            return ltp >= 401.00 && ltp <= 500.00 && data.getStrike_price() % 100 == 0; // Check LTP and strike price
                        })
                        .map(NiftyOptionChainDataDTO::getPut_options)
                        .max(Comparator.comparingDouble(o -> o.getMarket_data().getLtp()));
            }
            return putOption.orElse(null); // Return the found put option or null
        }
        return null; // Return null if transaction type does not match
    }

    public void placeBuyOrder(NiftyOptionDTO niftyOptionDTO, NiftyOptionMapping niftyOptionMapping, String token) throws IOException, InterruptedException, UnirestException {
        ObjectMapper objectMapper = new ObjectMapper();
        niftyCurrentInstrument = niftyOptionDTO.getInstrument_key();
        niftyCurrentOptionTradeHigh = 0.00;
        // place 1st market order
        String requestBody = "{"
                + "\"quantity\": " + niftyOptionMapping.getQuantity() * niftyOptionMapping.getNumberOfLots() + ","
                + "\"product\": \"D\","
                + "\"validity\": \"DAY\","
                + "\"price\": 0,"
                + "\"tag\": \"string\","
                + "\"instrument_token\": \"" + niftyOptionDTO.getInstrument_key() + "\","
                + "\"order_type\": \"MARKET\","
                + "\"transaction_type\": \"BUY\","
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
                .header("Authorization", token)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
        HttpResponse<String> placeOptionBuyOrderResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        JsonNode jsonNodeOrderDetails = objectMapper.readTree(placeOptionBuyOrderResponse.body());
        log.info("Placed order response " + placeOptionBuyOrderResponse.body());
        String statusOrderDetails = jsonNodeOrderDetails.get("status").asText();
        if (statusOrderDetails.equalsIgnoreCase("error")) {
            return;
        }

        OrderData orderData = objectMapper.readValue(placeOptionBuyOrderResponse.body(), OrderData.class);
        log.info("Order placed data : " + orderData);
        niftyOrderMapperRepository.save(NiftyOrderMapper.builder().orderId(orderData.getData().getOrderId()).orderType("BUY").build());

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
        log.info("Buy order details : " + placedMarketOrderResponse);

        // place initial target order
        if (placedMarketOrderResponse.getData().getOrderStatus().equalsIgnoreCase("complete")) {
            averagePrice = placedMarketOrderResponse.getData().getAveragePrice();
            log.info("Average price : " + averagePrice + " Target point : " + niftyOptionMapping.getProfitPoints());
            double targetPrice = Math.round((averagePrice + niftyOptionMapping.getProfitPoints()) * 20)/20.00;
            niftyOptionBuyPrice = averagePrice;
            niftyOptionInitialTargetPrice = targetPrice;
            niftyTrailSlPrice = averagePrice - niftyOptionMapping.getStopLossPriceRange();
            log.info("Target Price : " + targetPrice);
            int quantity = placedMarketOrderResponse.getData().getQuantity()/2;
            niftyRemainingQuantity = placedMarketOrderResponse.getData().getQuantity()/2;

            String url = "https://api-hft.upstox.com/v2/order/place";
            // Set up the request body
            String targetRequestBody = "{"
                            + "\"quantity\": " + quantity + ","
                            + "\"product\": \"D\","
                            + "\"validity\": \"DAY\","
                            + "\"price\": " + targetPrice + ","
                            + "\"tag\": \"string\","
                            + "\"instrument_token\": \"" + niftyOptionDTO.getInstrument_key() + "\","
                            + "\"order_type\": \"LIMIT\","
                            + "\"transaction_type\": \"SELL\","
                            + "\"disclosed_quantity\": 0,"
                            + "\"trigger_price\": " + targetPrice + ","
                            + "\"is_amo\": false"
                            + "}";
            log.info("Target Order Request : " + targetRequestBody);
            // Create the HttpClient
            httpClient = HttpClient.newHttpClient();

            // Create the HttpRequest
            HttpRequest targetRequest = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .header("Authorization", token)
                    .POST(HttpRequest.BodyPublishers.ofString(targetRequestBody))
                    .build();

                // Send the request and retrieve the response
            HttpResponse<String> response = httpClient.send(targetRequest, HttpResponse.BodyHandlers.ofString());

            jsonNodeOrderDetails = objectMapper.readTree(response.body());
            statusOrderDetails = jsonNodeOrderDetails.get("status").asText();
            if (statusOrderDetails.equalsIgnoreCase("error")) {
                return;
            }
            OrderData targetorderData = objectMapper.readValue(response.body(), OrderData.class);
            log.info("Target Order response : " + targetorderData);
            niftyOrderMapperRepository.save(NiftyOrderMapper.builder().orderId(targetorderData.getData().getOrderId()).orderType("SELL").build());
        }

    }

    public NiftyLtpResponseDTO fetchCmp(String token) throws IOException, InterruptedException {
        String nifty = "NSE_INDEX%7CNifty%2050";
        String url = "https://api.upstox.com/v2/market-quote/ltp?instrument_key=" + nifty;
        String acceptHeader = "application/json";

        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", acceptHeader)
                .header("Authorization", token)
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        ObjectMapper objectMapper = new ObjectMapper();
        log.info("Current market price received : " + response.body());
        return objectMapper.readValue(response.body(), NiftyLtpResponseDTO.class);
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
        log.info("Get all order details: " + orderDetailsResponse.body());
        return objectMapper.readValue(orderDetailsResponse.body(), AllOrderDetailsDto.class);
    }

    public void cancelAllOpenOrders() throws IOException, InterruptedException {
        //cancel all open orders
        String url = "https://api.upstox.com/v2/order/multi/cancel";

        // Replace with your actual values
        String acceptHeader = "application/json";

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", acceptHeader)
                .header("Authorization", atulSchedulerToken)
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        log.info("Response Code received from cancel all open orders : " + response.statusCode());
        log.info("Response Body received from cancel all open orders : " + response.body());

        niftyOrderMapperRepository.deleteAll();
    }

    public void squareOffAllPositions() throws UpstoxException, UnirestException, IOException, InterruptedException {
        // get all positions and square off it

        String url = "https://api.upstox.com/v2/order/positions/exit";

        // empty request body
        String requestBody = "";

        // Create the HttpClient
        HttpClient httpClient = HttpClient.newHttpClient();

        // Create the HttpRequest
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", atulSchedulerToken)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        // Send the request and retrieve the response
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // Print the response status code and body
        log.info("Response Code from square off all open position api: " + response.statusCode());
        log.info("Response Body square off all open position api: " + response.body());
        niftyOrderMapperRepository.deleteAll();
    }

    public List<Integer> getCallStrikes(double cmp) {
        List<Integer> callStrikes = new ArrayList<>();
        // Starting strike from the nearest multiple of 100
        for (int strike = (int) (cmp / 100) * 100; strike >= cmp - 100; strike -= 100) {
            if (strike <= cmp) {
                callStrikes.add(strike);
            }
        }
        // Ensure the list contains exactly 2 elements
        while (callStrikes.size() < 2) {
            callStrikes.add(callStrikes.get(callStrikes.size() - 1) - 100); // Add lower strikes
        }
        return callStrikes.subList(0, 2); // Only return the first 2 elements
    }

    public List<Integer> getPutStrikes(double cmp) {
        List<Integer> putStrikes = new ArrayList<>();
        // Starting strike from the nearest multiple of 100
        for (int strike = (int) (cmp / 100) * 100; strike <= cmp + 100; strike += 100) {
            if (strike >= cmp) {
                putStrikes.add(strike);
            }
        }
        // Ensure the list contains exactly 2 elements
        while (putStrikes.size() < 2) {
            putStrikes.add(putStrikes.get(putStrikes.size() - 1) + 100); // Add higher strikes
        }
        return putStrikes.subList(0, 2); // Only return the first 2 elements
    }
}
