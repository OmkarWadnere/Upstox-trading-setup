package com.upstox.production.script5.entity;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Script5OrderMapper {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String orderId;
}
