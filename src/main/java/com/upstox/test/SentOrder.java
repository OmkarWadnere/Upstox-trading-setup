package com.upstox.test;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@RestController
@RequestMapping("/sentOrder")
public class SentOrder {

    @PostMapping
    public String placeOrder() {
        String url = "https://api.upstox.com/v2/order/place";
        String token = "Bearer eyJ0eXAiOiJKV1QiLCJrZXlfaWQiOiJza192MS4wIiwiYWxnIjoiSFMyNTYifQ.eyJzdWIiOiIyOTMyNTQiLCJqdGkiOiI2NWI0ZjExOWY1NDU1ZDI4YWQ3MjliZGYiLCJpc011bHRpQ2xpZW50IjpmYWxzZSwiaXNBY3RpdmUiOnRydWUsInNjb3BlIjpbImludGVyYWN0aXZlIiwiaGlzdG9yaWNhbCJdLCJpYXQiOjE3MDYzNTcwMTcsImlzcyI6InVkYXBpLWdhdGV3YXktc2VydmljZSIsImV4cCI6MTcwNjM5MjgwMH0.H0B4X__taJoaDhHqgWZjkBnix5pS5BZNZewu6lanvw0";

        // Set up the request body
        String requestBody = "{"
                + "\"quantity\": 1,"
                + "\"product\": \"D\","
                + "\"validity\": \"DAY\","
                + "\"price\": 0,"
                + "\"tag\": \"string\","
                + "\"instrument_token\": \"NSE_EQ|INE669E01016\","
                + "\"order_type\": \"MARKET\","
                + "\"transaction_type\": \"BUY\","
                + "\"disclosed_quantity\": 0,"
                + "\"trigger_price\": 0,"
                + "\"is_amo\": false"
                + "}";

        // Create the HttpClient
        HttpClient httpClient = HttpClient.newHttpClient();

        // Create the HttpRequest
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", token)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        try {
            // Send the request and retrieve the response
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // Print the response status code and body
            System.out.println("Response Code: " + response.statusCode());
            System.out.println("Response Body: " + response.body());

        } catch (Exception e) {
            // Handle exceptions
            e.printStackTrace();
        }
        return "success";
    }

}
