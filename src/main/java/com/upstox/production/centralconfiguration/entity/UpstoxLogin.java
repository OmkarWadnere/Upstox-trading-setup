package com.upstox.production.centralconfiguration.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UpstoxLogin {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    private String email;
    private String user_name;
    @Column(length = 6)
    private String user_id;
    private String user_type;
    private Boolean is_active;
    @Column(length = 500)
    private String access_token;
}
