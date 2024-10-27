package com.upstox.production.nifty.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.upstox.production.centralconfiguration.dto.GetPositionResponseDto;
import com.upstox.production.centralconfiguration.dto.OrderRequestDto;
import com.upstox.production.centralconfiguration.excpetion.UpstoxException;
import com.upstox.production.centralconfiguration.repository.UpstoxLoginRepository;
import com.upstox.production.nifty.dto.NiftyLtpResponseDTO;
import com.upstox.production.nifty.dto.NiftyOptionChainResponseDTO;
import com.upstox.production.nifty.dto.NiftyOptionDTO;
import com.upstox.production.nifty.entity.NiftyOptionMapping;
import com.upstox.production.nifty.repository.NiftyNextOptionMapperRepository;
import com.upstox.production.nifty.repository.NiftyOptionMappingRepository;
import com.upstox.production.nifty.service.helper.NiftyOrderHelper;
import com.upstox.production.nifty.utility.NiftyUtility;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.upstox.production.centralconfiguration.utility.CentralUtility.atulSchedulerToken;
import static com.upstox.production.nifty.utility.NiftyUtility.*;

@Service
@PropertySource("classpath:data.properties")
public class NiftyOrderService {

    private static final Log log = LogFactory.getLog(NiftyOrderService.class);

    private static final Double MULTIPLIER = 0.05;

    @Autowired
    private Environment environment;

    @Autowired
    private UpstoxLoginRepository upstoxLoginRepository;

    @Autowired
    private NiftyOptionMappingRepository niftyOptionMappingRepository;

    @Autowired
    private NiftyNextOptionMapperRepository niftyNextOptionMapperRepository;

    @Autowired
    private NiftyOrderHelper niftyOrderHelper;

    public void buyOrderExecution(String requestData) throws UpstoxException, IOException, InterruptedException, UnirestException, URISyntaxException {
        isNiftyMainExecutionRunning = true;
        LocalTime now = LocalTime.now();
        if (now.isAfter(LocalTime.of(15,24, 30))) {
            isNiftyMainExecutionRunning = false;
            return;
        }

        // Process buy order Request Data
        OrderRequestDto orderRequestDto = processBuyOrderRequestData(requestData);
        // fetch cmp of  nifty
        NiftyLtpResponseDTO niftyLtpResponseDTO = niftyOrderHelper.fetchCmp(atulSchedulerToken);
        log.info(" nifty CMP: " + niftyLtpResponseDTO);
        double ltp = niftyLtpResponseDTO.getData().get("NSE_INDEX:Nifty 50").getLast_price();
        //get list of eligible call and put options
        List<Integer> callStrikes = niftyOrderHelper.getCallStrikes(ltp);
        List<Integer> putStrikes = niftyOrderHelper.getPutStrikes(ltp);
        log.info("Supported Call: " + callStrikes);
        log.info("Supported Put: " + putStrikes);
        Optional<NiftyOptionMapping> optionalNiftyFutureMapping = niftyOptionMappingRepository.findByInstrumentToken(orderRequestDto.getInstrument_name());
        if (optionalNiftyFutureMapping.isEmpty()) {
            isNiftyMainExecutionRunning = false;
            throw new UpstoxException("There is no future mapping for : " + orderRequestDto.getInstrument_name());
        }
        if (atulSchedulerToken.isEmpty() || atulSchedulerToken.length() == 0) {
            atulSchedulerToken = "Bearer " + upstoxLoginRepository.findByEmail(environment.getProperty("email_id")).get().getAccess_token();
        }
        log.info("Option Mapping details : " + optionalNiftyFutureMapping);
        NiftyOptionDTO niftyOptionDTO = null;
        log.info("order request data : " + orderRequestDto);
        log.info("Strike price : " + orderRequestDto.getStrikePrice());
        log.info("Nifty call flag : " + NiftyUtility.niftyCallOptionFlag + " Put flag : " + NiftyUtility.niftyPutOptionFlag);
        log.info("Thread details : " + isNiftyMainExecutionRunning);
        if (orderRequestDto.getOptionType().equals("CALL") && callStrikes.contains(orderRequestDto.getStrikePrice())) {
            if (orderRequestDto.getTransaction_type().equals("BUY") && !NiftyUtility.niftyCallOptionFlag) {
                if (now.isAfter(LocalTime.of(9, 15)) && now.isBefore(LocalTime.of(9, 30)) && niftyMorningTradeCounter < 1) {
                    niftyMorningTradeCounter++;
                    NiftyUtility.niftyCallOptionFlag = true;
                    NiftyUtility.niftyPutOptionFlag = false;
                    isNiftyMainExecutionRunning = false;
                    log.info("We are not taking trade because its first trade of the day at time : " + LocalDateTime.now());
                    return;
                }
                log.info("Here1");
                NiftyUtility.niftyCallOptionFlag = true;
                NiftyUtility.niftyPutOptionFlag = false;
                niftyOrderHelper.cancelAllOpenOrders();
                log.info("Here2");
                niftyOrderHelper.squareOffAllPositions();
                log.info("Here3");
                NiftyOptionChainResponseDTO niftyOptionChainResponseDTO = niftyOrderHelper.getOptionChain(optionalNiftyFutureMapping.get(), atulSchedulerToken);
                log.info("Option chain details : " + niftyOptionChainResponseDTO);
                niftyOptionDTO = niftyOrderHelper.filterNiftyOptionStrike(niftyOptionChainResponseDTO, "CALL_BUY");
                if (niftyOptionDTO == null) {
                    isNiftyMainExecutionRunning = false;
                    throw new UpstoxException("There is no option in our range");
                }
                log.info("Selected strike : " + niftyOptionDTO);
                niftyOrderHelper.placeBuyOrder(niftyOptionDTO, optionalNiftyFutureMapping.get(), atulSchedulerToken);
            } else if (orderRequestDto.getTransaction_type().equals("SELL") && !NiftyUtility.niftyPutOptionFlag){
                if (now.isAfter(LocalTime.of(9, 15)) && now.isBefore(LocalTime.of(9, 30)) && niftyMorningTradeCounter < 1) {
                    niftyMorningTradeCounter++;
                    NiftyUtility.niftyCallOptionFlag = false;
                    NiftyUtility.niftyPutOptionFlag = true;
                    isNiftyMainExecutionRunning = false;
                    log.info("We are not taking trade because its first trade of the day at time : " + LocalDateTime.now());
                    return;
                }
                log.info("Here1");
                NiftyUtility.niftyCallOptionFlag = false;
                NiftyUtility.niftyPutOptionFlag = true;
                niftyOrderHelper.cancelAllOpenOrders();
                log.info("Here2");
                niftyOrderHelper.squareOffAllPositions();
                log.info("Here3");
                NiftyOptionChainResponseDTO niftyOptionChainResponseDTO = niftyOrderHelper.getOptionChain(optionalNiftyFutureMapping.get(), atulSchedulerToken);
                niftyOptionDTO = niftyOrderHelper.filterNiftyOptionStrike(niftyOptionChainResponseDTO, "PUT_BUY");
                log.info("Option chain details : " + niftyOptionChainResponseDTO);
                if (niftyOptionDTO == null) {
                    isNiftyMainExecutionRunning = false;
                    throw new UpstoxException("There is no option in our range");
                }
                log.info("Selected strike : " + niftyOptionDTO);
                niftyOrderHelper.placeBuyOrder(niftyOptionDTO, optionalNiftyFutureMapping.get(), atulSchedulerToken);
            }
        } else if (orderRequestDto.getOptionType().equals("PUT") && putStrikes.contains(orderRequestDto.getStrikePrice())) {
            if (orderRequestDto.getTransaction_type().equals("BUY") && !NiftyUtility.niftyPutOptionFlag) {
                if (now.isAfter(LocalTime.of(9, 15)) && now.isBefore(LocalTime.of(9, 30)) && niftyMorningTradeCounter < 1) {
                    niftyMorningTradeCounter++;
                    NiftyUtility.niftyCallOptionFlag = false;
                    NiftyUtility.niftyPutOptionFlag = true;
                    isNiftyMainExecutionRunning = false;
                    log.info("We are not taking trade because its first trade of the day at time : " + LocalDateTime.now());
                    return;
                }
                log.info("Here1");
                NiftyUtility.niftyCallOptionFlag = false;
                NiftyUtility.niftyPutOptionFlag = true;
                niftyOrderHelper.cancelAllOpenOrders();
                log.info("Here2");
                niftyOrderHelper.squareOffAllPositions();
                log.info("Here3");
                NiftyOptionChainResponseDTO niftyOptionChainResponseDTO = niftyOrderHelper.getOptionChain(optionalNiftyFutureMapping.get(), atulSchedulerToken);

                niftyOptionDTO = niftyOrderHelper.filterNiftyOptionStrike(niftyOptionChainResponseDTO, "PUT_BUY");
                log.info("Option chain details : " + niftyOptionChainResponseDTO);
                if (niftyOptionDTO == null) {
                    isNiftyMainExecutionRunning = false;
                    throw new UpstoxException("There is no option in our range");
                }
                log.info("Selected strike : " + niftyOptionDTO);
                niftyOrderHelper.placeBuyOrder(niftyOptionDTO, optionalNiftyFutureMapping.get(), atulSchedulerToken);

            } else if (orderRequestDto.getTransaction_type().equals("SELL") && !NiftyUtility.niftyCallOptionFlag) {
                if (now.isAfter(LocalTime.of(9, 15)) && now.isBefore(LocalTime.of(9, 30)) && niftyMorningTradeCounter < 1) {
                    niftyMorningTradeCounter++;
                    NiftyUtility.niftyCallOptionFlag = true;
                    NiftyUtility.niftyPutOptionFlag = false;
                    isNiftyMainExecutionRunning = false;
                    log.info("We are not taking trade because its first trade of the day at time : " + LocalDateTime.now());
                    return;
                }
                log.info("Here1");
                NiftyUtility.niftyCallOptionFlag = true;
                NiftyUtility.niftyPutOptionFlag = false;
                niftyOrderHelper.cancelAllOpenOrders();
                log.info("Here2");
                niftyOrderHelper.squareOffAllPositions();
                log.info("Here3");
                NiftyOptionChainResponseDTO niftyOptionChainResponseDTO = niftyOrderHelper.getOptionChain(optionalNiftyFutureMapping.get(), atulSchedulerToken);
                niftyOptionDTO = niftyOrderHelper.filterNiftyOptionStrike(niftyOptionChainResponseDTO, "CALL_BUY");
                log.info("Option chain details : " + niftyOptionChainResponseDTO);
                if (niftyOptionDTO == null) {
                    isNiftyMainExecutionRunning = false;
                    throw new UpstoxException("There is no option in our range");
                }
                log.info("Selected strike : " + niftyOptionDTO);
                niftyOrderHelper.placeBuyOrder(niftyOptionDTO, optionalNiftyFutureMapping.get(), atulSchedulerToken);
            }
        }
        isNiftyMainExecutionRunning = false;
    }

    public NiftyOptionMapping getFutureMapping(OrderRequestDto orderRequestDto) throws UpstoxException {
        Optional<NiftyOptionMapping> optionalFutureMappingSymbolName = niftyOptionMappingRepository.findBySymbolName(orderRequestDto.getInstrument_name());
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

   private  OrderRequestDto processBuyOrderRequestData(String requestData) throws JsonProcessingException {
       ObjectMapper objectMapper = new ObjectMapper();
       JsonNode jsonNode = objectMapper.readTree(requestData);
        log.info("Buy Order Request process has started for the data : " + jsonNode.toString());
       // Continue with your processing
       String optionType = jsonNode.get("optionType").asText();
       int strikePrice = jsonNode.get("strikePrice").asInt();
       int quantity = jsonNode.get("quantity").asInt();
       String instrumentName = jsonNode.get("instrument_name").asText();
       String orderType = jsonNode.get("order_type").asText();
       double price = jsonNode.get("price").asDouble();
       // If you need to convert order_name into a separate JsonNode with key-value pairs
       String[] parts = jsonNode.get("order_name").toString().replace("\"", "").split(" ");
       Map<String, String> map = new HashMap<>();
       for (String part : parts) {
           String[] subParts = part.split(":");
           map.put(subParts[0], subParts[1]);
       }

       return OrderRequestDto.builder()
               .strikePrice(strikePrice)
               .optionType(optionType)
               .quantity(quantity)
               .price(Math.round(price*100.0)/100.0)
               .instrument_name(instrumentName)
               .order_type("LIMIT")
               .transaction_type(map.get("TYPE").trim().equals("LE") ? "BUY" : "SELL")
               .entryPrice(Double.parseDouble(map.get("entryPrice")))
               .build();
   }
}
