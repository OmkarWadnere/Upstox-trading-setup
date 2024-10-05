package com.upstox.production.banknifty.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Data
@Getter
@ToString
public class NiftyBankData {
    private double last_price;       // Last traded price
    private double open_price;       // Opening price
    private double high_price;       // Highest price of the day
    private double low_price;        // Lowest price of the day
    private double previous_close;   // Previous close price
    private double change;           // Change in price
    private double percent_change;   // Percentage change in price
    private long volume;             // Volume of shares traded
    private String timestamp;        // Timestamp of the last update

    @JsonProperty("instrument_token") // This maps the JSON field to the Java field
    private String instrumentToken;    // Instrument token field
}