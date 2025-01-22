package com.bfp.auth;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class BFPUser {
    private String username;
    private Map<String, String> userAttributes;
}
