package com.upstox.production.centralconfiguration.enums;

import com.upstox.production.centralconfiguration.excpetion.UpstoxException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@AllArgsConstructor
@Getter
public enum UserAccess {

    TRADE_ACCESS("Trade_Access"),
    FETCH_DATA_ACCESS("Fetch_Data_Access");

    private String accessType;

    public static UserAccess getAccess(String accessType) throws UpstoxException {
        return Arrays.stream(UserAccess.values()).filter(data -> data.getAccessType().equals(accessType))
                .findAny()
                .orElseThrow(() -> new UpstoxException("No Record found for access type"));
    }
}
