package com.upstox.production.dto;

import lombok.*;

@Getter
@Builder
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpstoxLoginDto {

    private Integer id;
    private String email;
    private String user_name;
    private Integer user_id;
    private String user_type;
    private Boolean is_active;
    private String access_token;


}
