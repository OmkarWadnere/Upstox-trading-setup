package com.upstox.production.banknifty.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.upstox.production.banknifty.dto.BankNiftyLtpResponseDTO;
import com.upstox.production.banknifty.dto.BankNiftyOptionChainResponseDTO;
import com.upstox.production.banknifty.dto.BankNiftyOptionDTO;
import com.upstox.production.banknifty.service.helper.BankNiftyOrderHelper;
import com.upstox.production.banknifty.utility.BankNiftyUtility;
import com.upstox.production.centralconfiguration.dto.*;
import com.upstox.production.banknifty.entity.BankNiftyOptionMapping;
import com.upstox.production.banknifty.repository.BankNiftyOptionMappingRepository;
import com.upstox.production.banknifty.repository.BankNiftyNextOptionMapperRepository;
import com.upstox.production.centralconfiguration.repository.UpstoxLoginRepository;
import com.upstox.production.centralconfiguration.excpetion.UpstoxException;
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
import java.util.*;

import static com.upstox.production.banknifty.utility.BankNiftyUtility.*;

@Service
@PropertySource("classpath:data.properties")
public class BankNiftyOrderService {

    private static final Log log = LogFactory.getLog(BankNiftyOrderService.class);

    private static final Double MULTIPLIER = 0.05;

    @Autowired
    private Environment environment;

    @Autowired
    private UpstoxLoginRepository upstoxLoginRepository;

    @Autowired
    private BankNiftyOptionMappingRepository bankNiftyOptionMappingRepository;

    @Autowired
    private BankNiftyNextOptionMapperRepository bankNiftyNextOptionMapperRepository;

    @Autowired
    private BankNiftyOrderHelper bankNiftyOrderHelper;

    public void buyOrderExecution(String requestData) throws UpstoxException, IOException, InterruptedException, UnirestException, URISyntaxException {
        isBankNiftyMainExecutionRunning = true;
        LocalTime now = LocalTime.now();
        if (now.isAfter(LocalTime.of(9, 15)) && now.isBefore(LocalTime.of(9, 30)) && bankNiftyMorningTradeCounter < 1) {
            bankNiftyMorningTradeCounter++;
            isBankNiftyMainExecutionRunning = false;
            log.info("We are not taking tade because its first trade of the day at time : " + LocalDateTime.now());
        }
        if (now.isAfter(LocalTime.of(15,24, 30))) {
            isBankNiftyMainExecutionRunning = false;
            return;
        }

        // Process buy order Request Data
        OrderRequestDto orderRequestDto = processBuyOrderRequestData(requestData);
        // fetch cmp of bank nifty
        BankNiftyLtpResponseDTO bankNiftyLtpResponseDTO = bankNiftyOrderHelper.fetchCmp(BankNiftyUtility.schedulerToken);
        log.info("Bank nifty CMP: " + bankNiftyLtpResponseDTO);
        double ltp = bankNiftyLtpResponseDTO.getData().get("NSE_INDEX:Nifty Bank").getLast_price();
        //get list of eligible call and put options
        List<Integer> callStrikes = bankNiftyOrderHelper.getCallStrikes(ltp);
        List<Integer> putStrikes = bankNiftyOrderHelper.getPutStrikes(ltp);
        log.info("Supported Call: " + callStrikes);
        log.info("Supported Put: " + putStrikes);
        Optional<BankNiftyOptionMapping> optionalBankNiftyFutureMapping = bankNiftyOptionMappingRepository.findByInstrumentToken(orderRequestDto.getInstrument_name());
        if (optionalBankNiftyFutureMapping.isEmpty()) {
            isBankNiftyMainExecutionRunning = false;
            throw new UpstoxException("There is no future mapping for : " + orderRequestDto.getInstrument_name());
        }
        if (schedulerToken.isEmpty() || schedulerToken.length() == 0) {
            schedulerToken = "Bearer " + upstoxLoginRepository.findByEmail(environment.getProperty("email_id")).get();
        }
        log.info("Option Mapping details : " + optionalBankNiftyFutureMapping);
        BankNiftyOptionDTO bankNiftyOptionDTO = null;
        log.info("order request data : " + orderRequestDto);
        log.info("Strike price : " + orderRequestDto.getStrikePrice());
        log.info("Bank Nifty call flag : " + BankNiftyUtility.bankNiftyCallOptionFlag + " Put flag : " + BankNiftyUtility.bankNiftyPutOptionFlag);
        if (orderRequestDto.getOptionType().equals("CALL") && callStrikes.contains(orderRequestDto.getStrikePrice())) {
            if (orderRequestDto.getTransaction_type().equals("BUY") && !BankNiftyUtility.bankNiftyCallOptionFlag) {
                BankNiftyUtility.bankNiftyCallOptionFlag = true;
                BankNiftyUtility.bankNiftyPutOptionFlag = false;
                bankNiftyOrderHelper.cancelAllOpenOrders();
                bankNiftyOrderHelper.squareOffAllPositions();
                BankNiftyOptionChainResponseDTO bankNiftyOptionChainResponseDTO = bankNiftyOrderHelper.getOptionChain(optionalBankNiftyFutureMapping.get(), BankNiftyUtility.schedulerToken);
                log.info("Option chain details : " + bankNiftyOptionChainResponseDTO);
                bankNiftyOptionDTO = bankNiftyOrderHelper.filterBankNiftyOptionStrike(bankNiftyOptionChainResponseDTO, "CALL_BUY");
                if (bankNiftyOptionDTO == null) {
                    isBankNiftyMainExecutionRunning = false;
                    throw new UpstoxException("There is no option in our range");
                }
                log.info("Selected strike : " + bankNiftyOptionDTO);
                bankNiftyOrderHelper.placeBuyOrder(bankNiftyOptionDTO, optionalBankNiftyFutureMapping.get(), BankNiftyUtility.schedulerToken);
            } else if (orderRequestDto.getTransaction_type().equals("SELL") && !BankNiftyUtility.bankNiftyPutOptionFlag){
                BankNiftyUtility.bankNiftyCallOptionFlag = false;
                BankNiftyUtility.bankNiftyPutOptionFlag = true;
                bankNiftyOrderHelper.cancelAllOpenOrders();
                bankNiftyOrderHelper.squareOffAllPositions();
                BankNiftyOptionChainResponseDTO bankNiftyOptionChainResponseDTO = bankNiftyOrderHelper.getOptionChain(optionalBankNiftyFutureMapping.get(), BankNiftyUtility.schedulerToken);
                bankNiftyOptionDTO = bankNiftyOrderHelper.filterBankNiftyOptionStrike(bankNiftyOptionChainResponseDTO, "PUT_BUY");
                log.info("Option chain details : " + bankNiftyOptionChainResponseDTO);
                if (bankNiftyOptionDTO == null) {
                    isBankNiftyMainExecutionRunning = false;
                    throw new UpstoxException("There is no option in our range");
                }
                log.info("Selected strike : " + bankNiftyOptionDTO);
                bankNiftyOrderHelper.placeBuyOrder(bankNiftyOptionDTO, optionalBankNiftyFutureMapping.get(), BankNiftyUtility.schedulerToken);
            }
        } else if (orderRequestDto.getOptionType().equals("PUT") && putStrikes.contains(orderRequestDto.getStrikePrice())) {
            if (orderRequestDto.getTransaction_type().equals("BUY") && !BankNiftyUtility.bankNiftyPutOptionFlag) {
                BankNiftyUtility.bankNiftyCallOptionFlag = false;
                BankNiftyUtility.bankNiftyPutOptionFlag = true;
                bankNiftyOrderHelper.cancelAllOpenOrders();
                bankNiftyOrderHelper.squareOffAllPositions();
                BankNiftyOptionChainResponseDTO bankNiftyOptionChainResponseDTO = bankNiftyOrderHelper.getOptionChain(optionalBankNiftyFutureMapping.get(), BankNiftyUtility.schedulerToken);

                bankNiftyOptionDTO = bankNiftyOrderHelper.filterBankNiftyOptionStrike(bankNiftyOptionChainResponseDTO, "PUT_BUY");
                log.info("Option chain details : " + bankNiftyOptionChainResponseDTO);
                if (bankNiftyOptionDTO == null) {
                    isBankNiftyMainExecutionRunning = false;
                    throw new UpstoxException("There is no option in our range");
                }
                log.info("Selected strike : " + bankNiftyOptionDTO);
                bankNiftyOrderHelper.placeBuyOrder(bankNiftyOptionDTO, optionalBankNiftyFutureMapping.get(), BankNiftyUtility.schedulerToken);

            } else if (orderRequestDto.getTransaction_type().equals("SELL") && !BankNiftyUtility.bankNiftyCallOptionFlag) {
                BankNiftyUtility.bankNiftyCallOptionFlag = true;
                BankNiftyUtility.bankNiftyPutOptionFlag = false;
                bankNiftyOrderHelper.cancelAllOpenOrders();
                bankNiftyOrderHelper.squareOffAllPositions();
                BankNiftyOptionChainResponseDTO bankNiftyOptionChainResponseDTO = bankNiftyOrderHelper.getOptionChain(optionalBankNiftyFutureMapping.get(), BankNiftyUtility.schedulerToken);
                bankNiftyOptionDTO = bankNiftyOrderHelper.filterBankNiftyOptionStrike(bankNiftyOptionChainResponseDTO, "CALL_BUY");
                log.info("Option chain details : " + bankNiftyOptionChainResponseDTO);
                if (bankNiftyOptionDTO == null) {
                    isBankNiftyMainExecutionRunning = false;
                    throw new UpstoxException("There is no option in our range");
                }
                log.info("Selected strike : " + bankNiftyOptionDTO);
                bankNiftyOrderHelper.placeBuyOrder(bankNiftyOptionDTO, optionalBankNiftyFutureMapping.get(), BankNiftyUtility.schedulerToken);
            }
        }
        isBankNiftyMainExecutionRunning = false;
    }

    public BankNiftyOptionMapping getFutureMapping(OrderRequestDto orderRequestDto) throws UpstoxException {
        Optional<BankNiftyOptionMapping> optionalFutureMappingSymbolName = bankNiftyOptionMappingRepository.findBySymbolName(orderRequestDto.getInstrument_name());
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
               .price(Double.parseDouble(map.get("entryPrice")))
               .instrument_name(instrumentName)
               .order_type("LIMIT")
               .transaction_type(map.get("TYPE").trim().equals("LE") ? "BUY" : "SELL")
               .entryPrice(Double.parseDouble(map.get("entryPrice")))
               .build();
   }
}
