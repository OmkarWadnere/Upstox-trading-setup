package com.upstox.production.nifty.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.upstox.production.centralconfiguration.dto.OrderRequestDto;
import com.upstox.production.centralconfiguration.excpetion.UpstoxException;
import com.upstox.production.centralconfiguration.mails.ApplicationMailSender;
import com.upstox.production.centralconfiguration.repository.TradeAccessUpstoxLoginRepository;
import com.upstox.production.nifty.dto.NiftyOptionChainResponseDTO;
import com.upstox.production.nifty.dto.NiftyOptionDTO;
import com.upstox.production.nifty.entity.NiftyOptionMapping;
import com.upstox.production.nifty.repository.NiftyNextOptionMapperRepository;
import com.upstox.production.nifty.repository.NiftyOptionMappingRepository;
import com.upstox.production.nifty.service.helper.NiftyOrderHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import static com.upstox.production.centralconfiguration.utility.CentralUtility.atulSchedulerToken;
import static com.upstox.production.centralconfiguration.utility.CentralUtility.tradingUserEmailId;
import static com.upstox.production.nifty.enums.OrderType.BUY_ENTRY;
import static com.upstox.production.nifty.enums.OrderType.BUY_EXIT;
import static com.upstox.production.nifty.enums.OrderType.SELL_ENTRY;
import static com.upstox.production.nifty.enums.OrderType.SELL_EXIT;
import static com.upstox.production.nifty.utility.NiftyUtility.currentParentInstrument;
import static com.upstox.production.nifty.utility.NiftyUtility.currentTradeType;
import static com.upstox.production.nifty.utility.NiftyUtility.isNiftyMainExecutionRunning;
import static com.upstox.production.nifty.utility.NiftyUtility.maxDrawDown;

@Service
@PropertySource("classpath:data.properties")
public class NiftyOrderService {

    private static final Log log = LogFactory.getLog(NiftyOrderService.class);
    private final BlockingQueue<OrderRequestDto> requestQueue = new LinkedBlockingQueue<>();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Autowired
    private Environment environment;

    @Autowired
    private TradeAccessUpstoxLoginRepository tradeAccessUpstoxLoginRepository;

    @Autowired
    private NiftyOptionMappingRepository niftyOptionMappingRepository;

    @Autowired
    private NiftyNextOptionMapperRepository niftyNextOptionMapperRepository;

    @Autowired
    private NiftyOrderHelper niftyOrderHelper;

    @Autowired
    private ApplicationMailSender applicationMailSender;

    @PostConstruct
    public void init() {
        executorService.submit(() -> {
            while (true) {
                try {
                    OrderRequestDto orderRequestDto = requestQueue.take();
                    processOrder(orderRequestDto);
                } catch (InterruptedException | UnirestException | IOException | URISyntaxException |
                         UpstoxException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
    }

    public void addOrderToQueue(String requestData) throws IOException, InterruptedException, UpstoxException {
        isNiftyMainExecutionRunning = true;
        LocalTime now = LocalTime.now();
        if (now.isAfter(LocalTime.of(15, 24, 30))) {
            isNiftyMainExecutionRunning = false;
            return;
        }

        // Process order Request Data
        OrderRequestDto orderRequestDto = processOrderRequestData(requestData);
        Optional<NiftyOptionMapping> optionalNiftyFutureMapping = niftyOptionMappingRepository.findByInstrumentToken(orderRequestDto.getInstrument_name());
        if (optionalNiftyFutureMapping.isEmpty()) {
            isNiftyMainExecutionRunning = false;
            applicationMailSender.sendMail("There is no future mapping for : " + orderRequestDto.getInstrument_name(), "No Future Mapping");
            throw new UpstoxException("There is no future mapping for : " + orderRequestDto.getInstrument_name());
        }
        if (atulSchedulerToken.isEmpty() || atulSchedulerToken.length() == 0) {
            atulSchedulerToken = "Bearer " + tradeAccessUpstoxLoginRepository.findByEmail(tradingUserEmailId).get().getAccess_token();
        }
        log.info("Option Mapping details : " + optionalNiftyFutureMapping);
        log.info("order requestData data : " + orderRequestDto);
        log.info("Strike price : " + orderRequestDto.getStrikePrice());
        log.info("Thread details : " + isNiftyMainExecutionRunning);
        if (orderRequestDto.getTransaction_type().equals(BUY_ENTRY.getOrderType())
                || orderRequestDto.getTransaction_type().equals(BUY_EXIT.getOrderType())
                || orderRequestDto.getTransaction_type().equals(SELL_ENTRY.getOrderType())
                || orderRequestDto.getTransaction_type().equals(SELL_EXIT.getOrderType())) {
            requestQueue.add(orderRequestDto);
        } else {
            log.error("Received wrong entry type : " + orderRequestDto);
        }

    }

    private synchronized void processOrder(OrderRequestDto orderRequestDto)
            throws UnirestException, IOException, URISyntaxException, InterruptedException, UpstoxException {
        NiftyOptionDTO niftyOptionDTO = null;
        Optional<NiftyOptionMapping> optionalNiftyOptionMapping =
                niftyOptionMappingRepository.findByInstrumentToken(orderRequestDto.getInstrument_name());

        if (orderRequestDto.getTransaction_type().equals(BUY_ENTRY.getOrderType())) {
            maxDrawDown = 0.00;
            currentTradeType = BUY_ENTRY.getOrderType();
            currentParentInstrument = orderRequestDto.getInstrument_name();
            log.info("Buy Entry order process initiated");
            niftyOrderHelper.cancelAllOpenOrders();
            niftyOrderHelper.squareOffAllPositions();
            if (optionalNiftyOptionMapping.isEmpty()) {
                throw new UpstoxException("There is not data present in current option mapping");
            }
            NiftyOptionChainResponseDTO niftyOptionChainResponseDTO = niftyOrderHelper.getOptionChain(optionalNiftyOptionMapping.get(), atulSchedulerToken);
            log.info("Option chain details : " + niftyOptionChainResponseDTO);
            niftyOptionDTO = niftyOrderHelper.filterNiftyOptionStrike(niftyOptionChainResponseDTO, "CALL_BUY");
            if (niftyOptionDTO == null) {
                isNiftyMainExecutionRunning = false;
                log.error("There is no option in our range please check option range received for reference : " + niftyOptionChainResponseDTO);
                throw new UpstoxException("There is no option in our range");
            }
            log.info("Selected strike : " + niftyOptionDTO);
            niftyOrderHelper.placeBuyOrder(niftyOptionDTO, optionalNiftyOptionMapping.get(), atulSchedulerToken);
        } else if (orderRequestDto.getTransaction_type().equals(BUY_EXIT.getOrderType())) {
            maxDrawDown = 0.00;
            currentTradeType = "";
            log.info("Buy Exit order process initiated");
            niftyOrderHelper.cancelAllOpenOrders();
            niftyOrderHelper.squareOffAllPositions();
        } else if (orderRequestDto.getTransaction_type().equals(SELL_ENTRY.getOrderType())) {
            maxDrawDown = 0.00;
            currentTradeType = SELL_ENTRY.getOrderType();
            currentParentInstrument = orderRequestDto.getInstrument_name();
            log.info("Sell Entry order process initiated");
            niftyOrderHelper.cancelAllOpenOrders();
            niftyOrderHelper.squareOffAllPositions();
            if (optionalNiftyOptionMapping.isEmpty()) {
                throw new UpstoxException("There is not data present in current option mapping");
            }
            NiftyOptionChainResponseDTO niftyOptionChainResponseDTO = niftyOrderHelper.getOptionChain(optionalNiftyOptionMapping.get(), atulSchedulerToken);
            log.info("Option chain details : " + niftyOptionChainResponseDTO);
            niftyOptionDTO = niftyOrderHelper.filterNiftyOptionStrike(niftyOptionChainResponseDTO, "PUT_BUY");
            if (niftyOptionDTO == null) {
                isNiftyMainExecutionRunning = false;
                log.error("There is no option in our range please check option range received for reference : " + niftyOptionChainResponseDTO);
                throw new UpstoxException("There is no option in our range");
            }
            log.info("Selected strike : " + niftyOptionDTO);
            niftyOrderHelper.placeBuyOrder(niftyOptionDTO, optionalNiftyOptionMapping.get(), atulSchedulerToken);
        } else if (orderRequestDto.getTransaction_type().equals(SELL_EXIT.getOrderType())) {
            maxDrawDown = 0.00;
            currentTradeType = "";
            log.info("Sell Exit order process initiated");
            niftyOrderHelper.cancelAllOpenOrders();
            niftyOrderHelper.squareOffAllPositions();
        } else {
            log.info("There is some error in requested data");
        }
        isNiftyMainExecutionRunning = false;
    }

    private OrderRequestDto processOrderRequestData(String requestData) throws JsonProcessingException {
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
        String transaction_type = "NA";
        if (map.get("TYPE").trim().equals("LE")) {
            transaction_type = BUY_ENTRY.getOrderType();
        } else if (map.get("TYPE").trim().equals("LX")) {
            transaction_type = BUY_EXIT.getOrderType();
        } else if (map.get("TYPE").trim().equals("SE")) {
            transaction_type = SELL_ENTRY.getOrderType();
        } else if (map.get("TYPE").trim().equals("SX")) {
            transaction_type = SELL_EXIT.getOrderType();
        }
        return OrderRequestDto.builder()
                .strikePrice(strikePrice)
                .optionType(optionType)
                .quantity(quantity)
                .price(Math.round(price * 100.0) / 100.0)
                .instrument_name(instrumentName)
                .order_type("LIMIT")
                .transaction_type(transaction_type)
                .entryPrice(Double.parseDouble(map.get("entryPrice")))
                .build();
    }
}
