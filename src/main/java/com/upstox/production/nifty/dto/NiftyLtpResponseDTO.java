package com.upstox.production.nifty.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Data
@Getter
@ToString
public class NiftyLtpResponseDTO {
    private String status;
    @JsonProperty("data") // This maps the "data" field directly
    private Map<String, NiftyData> data; // Using Map to handle dynamic keys
}