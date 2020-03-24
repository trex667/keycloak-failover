package org.schreibvehler.jmeter;

public class TokenBox {
    private final String accessToken;
    private final String refreshToken;

    public TokenBox(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }
}
