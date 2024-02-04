package com.upstox.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/placeOrder")
public class GetSampleOrderFromTradingView {

    @PostMapping(value = "/tradingView")
    public String receivedOrder(@RequestHeader(HttpHeaders.CONTENT_TYPE) String contentType,
                                @RequestBody String payload) throws JsonProcessingException {
        System.out.println("Order Request body : " + payload);

        try {
            // Use Jackson's ObjectMapper to convert JSON string to JsonNode
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(payload);

            // Extract order_name as a separate JsonNode

            // Continue with your processing
            double price = jsonNode.get("price").asDouble();
            int quantity = jsonNode.get("quantity").asInt();
            String instrumentName = jsonNode.get("instrument_name").asText();
            String orderType = jsonNode.get("order_type").asText();
            String transactionType = jsonNode.get("transaction_type").asText();

            // Output the results
            System.out.println("Price: " + price);
            System.out.println("Quantity: " + quantity);
            System.out.println("Instrument Name: " + instrumentName);
            System.out.println("Order Type: " + orderType);
            System.out.println("Transaction Type: " + transactionType);
            System.out.println("Order Name: " + jsonNode.get("order_name"));

            // If you need to convert order_name into a separate JsonNode with key-value pairs
            String[] parts = jsonNode.get("order_name").toString().replace("\"", "").split(" ");
            Map<String, String> map = new HashMap<>();
            for (String part : parts) {
                String subParts[] = part.split(":");
                map.put(subParts[0], subParts[1]);
            }
            System.out.println("Order Name Details Node: " + parts);
            for (Map.Entry<String, String> entryMap: map.entrySet()) {
                System.out.println("Map data : " + entryMap.getKey() + " " + entryMap.getValue());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Success";
    }

}
