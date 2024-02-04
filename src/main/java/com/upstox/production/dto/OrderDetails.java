package com.upstox.production.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class OrderDetails {

        @JsonProperty("exchange")
        private String exchange;

        @JsonProperty("product")
        private String product;

        @JsonProperty("price")
        private double price;

        @JsonProperty("quantity")
        private int quantity;

        @JsonProperty("status")
        private String orderStatus;

        @JsonProperty("tag")
        private String tag;

        @JsonProperty("instrument_token")
        private String instrumentToken;

        @JsonProperty("placed_by")
        private String placedBy;

        @JsonProperty("trading_symbol")
        private String tradingSymbol;

        @JsonProperty("tradingsymbol")
        private String tradingSymbolAlt;

        @JsonProperty("order_type")
        private String orderType;

        @JsonProperty("validity")
        private String validity;

        @JsonProperty("trigger_price")
        private double triggerPrice;

        @JsonProperty("disclosed_quantity")
        private int disclosedQuantity;

        @JsonProperty("transaction_type")
        private String transactionType;

        @JsonProperty("average_price")
        private double averagePrice;

        @JsonProperty("filled_quantity")
        private int filledQuantity;

        @JsonProperty("pending_quantity")
        private int pendingQuantity;

        @JsonProperty("exchange_order_id")
        private String exchangeOrderId;

        @JsonProperty("parent_order_id")
        private String parentOrderId;

        @JsonProperty("order_id")
        private String orderId;

        @JsonProperty("variety")
        private String variety;

        @JsonProperty("order_timestamp")
        private LocalDateTime orderTimestamp;

        @JsonProperty("exchange_timestamp")
        private LocalDateTime exchangeTimestamp;

        @JsonProperty("is_amo")
        private boolean isAmo;

        @JsonProperty("order_request_id")
        private String orderRequestId;

        @JsonProperty("order_ref_id")
        private String orderRefId;
}