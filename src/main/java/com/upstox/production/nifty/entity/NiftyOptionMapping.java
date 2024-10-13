package com.upstox.production.nifty.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class NiftyOptionMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String instrumentToken;
    private LocalDate expiryDate;
    private String symbolName;
    private Integer quantity;
    private Integer numberOfLots;
    private Double profitPoints;
    private Integer averagingTimes;
    private Double averagingPointInterval;
}
