package com.upstox.test;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@RestController
@RequestMapping("/getOrderInfo")
public class GetOrderInfo {

    @GetMapping
    public String getOrderDetails() {
        String url = "https://api.upstox.com/v2/order/details";
        String accept = "application/json";
        String authorization = "Bearer eyJ0eXAiOiJKV1QiLCJrZXlfaWQiOiJza192MS4wIiwiYWxnIjoiSFMyNTYifQ.eyJzdWIiOiIyOTMyNTQiLCJqdGkiOiI2NWI0ZjExOWY1NDU1ZDI4YWQ3MjliZGYiLCJpc011bHRpQ2xpZW50IjpmYWxzZSwiaXNBY3RpdmUiOnRydWUsInNjb3BlIjpbImludGVyYWN0aXZlIiwiaGlzdG9yaWNhbCJdLCJpYXQiOjE3MDYzNTcwMTcsImlzcyI6InVkYXBpLWdhdGV3YXktc2VydmljZSIsImV4cCI6MTcwNjM5MjgwMH0.H0B4X__taJoaDhHqgWZjkBnix5pS5BZNZewu6lanvw0";
        String orderId = "240127000002434";

        try {
            HttpClient httpClient = HttpClient.newHttpClient();

            // Build the URI with query parameters
            URI uri = URI.create(url + "?order_id=" + orderId);

            // Build the request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .header("Accept", accept)
                    .header("Authorization", authorization)
                    .GET()
                    .build();

            // Send the request and get the response
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // Print the response
            System.out.println("Response Code: " + response.statusCode());
            System.out.println("Response: " + response.body());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "details received";
    }
}
