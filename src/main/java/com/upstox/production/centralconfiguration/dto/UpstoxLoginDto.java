package com.upstox.production.centralconfiguration.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Builder
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UpstoxLoginDto {

    private Integer id;
    private String email;
    private String user_name;
    private String user_id;
    private String user_type;
    private Boolean is_active;
    private String access_token;


}
