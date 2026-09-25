package com.tasnim.taskflow_api;

public class AccessTokenResponse {

    private final String accessToken;
    private final String tokenType;
    private final long expiresIn;

    public AccessTokenResponse(
            String accessToken,
            long expiresIn
    ) {
        this.accessToken = accessToken;
        this.tokenType = "Bearer";
        this.expiresIn = expiresIn;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public long getExpiresIn() {
        return expiresIn;
    }
}