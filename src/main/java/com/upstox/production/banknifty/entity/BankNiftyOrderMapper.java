package com.upstox.production.banknifty.entity;

import lombok.*;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class BankNiftyOrderMapper {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String orderId;
    private String orderType;
}
