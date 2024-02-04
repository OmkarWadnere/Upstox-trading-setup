package com.upstox.production.entity;

import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpstoxLogin {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    private String email;
    private String user_name;
    private Integer user_id;
    private String user_type;
    private Boolean is_active;
    @Column(length = 500)
    private String access_token;
}
