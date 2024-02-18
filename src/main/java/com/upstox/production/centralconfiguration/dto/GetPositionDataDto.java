package com.upstox.production.centralconfiguration.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class GetPositionDataDto {

        @JsonProperty("exchange")
        private String exchange;

        @JsonProperty("multiplier")
        private double multiplier;

        @JsonProperty("value")
        private double value;

        @JsonProperty("pnl")
        private double pnl;

        @JsonProperty("product")
        private String product;

        @JsonProperty("instrument_token")
        private String instrumentToken;

        @JsonProperty("average_price")
        private double averagePrice;

        @JsonProperty("buy_value")
        private double buyValue;

        @JsonProperty("overnight_quantity")
        private int overnightQuantity;

        @JsonProperty("day_buy_value")
        private double dayBuyValue;

        @JsonProperty("day_buy_price")
        private double dayBuyPrice;

        @JsonProperty("overnight_buy_amount")
        private double overnightBuyAmount;

        @JsonProperty("overnight_buy_quantity")
        private int overnightBuyQuantity;

        @JsonProperty("day_buy_quantity")
        private int dayBuyQuantity;

        @JsonProperty("day_sell_value")
        private double daySellValue;

        @JsonProperty("day_sell_price")
        private double daySellPrice;

        @JsonProperty("overnight_sell_amount")
        private double overnightSellAmount;

        @JsonProperty("overnight_sell_quantity")
        private int overnightSellQuantity;

        @JsonProperty("day_sell_quantity")
        private int daySellQuantity;

        @JsonProperty("quantity")
        private int quantity;

        @JsonProperty("last_price")
        private double lastPrice;

        @JsonProperty("unrealised")
        private double unrealised;

        @JsonProperty("realised")
        private double realised;

        @JsonProperty("sell_value")
        private double sellValue;

        @JsonProperty("trading_symbol")
        private String tradingSymbol;

        @JsonProperty("tradingsymbol")
        private String tradingSymbolAlt;

        @JsonProperty("close_price")
        private double closePrice;

        @JsonProperty("buy_price")
        private double buyPrice;

        @JsonProperty("sell_price")
        private double sellPrice;

}