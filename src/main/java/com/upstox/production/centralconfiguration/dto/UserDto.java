package com.upstox.production.centralconfiguration.dto;

import com.upstox.production.centralconfiguration.enums.UserAccess;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
public class UserDto {

    private String clientId;
    private String clientSecrete;
    private String emailId;
    private UserAccess userAccessType;
}
