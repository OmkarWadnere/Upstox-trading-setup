package com.upstox.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.websocket.server.PathParam;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/redirect")
public class RedirectUrl {

    @GetMapping("/here")
    public ResponseEntity<String> redirected(@RequestParam String code) throws IOException {
        System.out.println("here we came " +code);
        String apiUrl = "https://api.upstox.com/v2/login/authorization/token";
        HttpURLConnection con = (HttpURLConnection) new java.net.URL(apiUrl).openConnection();

        // Set the request method
        con.setRequestMethod("POST");

        // Set the request headers
        con.setRequestProperty("accept", "application/json");
        con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        // Enable input/output streams
        con.setDoOutput(true);
                // Set the request data
                String data = "code="+ code +
                "&client_id=e2809017-3a4e-4821-97ca-c21c63cd6082" +
                "&client_secret=1zb1hyt7ck" +
                "&redirect_uri=http://localhost:8080/redirect/here" +
                "&grant_type=authorization_code";

        // Write the request data to the output stream
        try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
            wr.write(data.getBytes(StandardCharsets.UTF_8));
            wr.flush();
        }

        // Get the response code
        int responseCode = con.getResponseCode();
        System.out.println("Response Code: " + responseCode);

        // Read the response
        try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }

            // Print the response
            System.out.println(response.toString());
        }
        return new ResponseEntity<>("we have redirected " + code, HttpStatus.OK);
    }
}
