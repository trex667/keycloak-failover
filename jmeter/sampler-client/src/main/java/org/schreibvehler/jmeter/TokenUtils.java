package org.schreibvehler.jmeter;

import javax.json.Json;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Objects;

public class TokenUtils {

    public static String getPayload(String base64EncodedToken) {
        String encodedPayload = getEncodedPayload(base64EncodedToken);
        return new String(Base64.getDecoder().decode(encodedPayload), StandardCharsets.UTF_8);
    }

    private static String getEncodedPayload(String base64EncodedToken) {
        Objects.requireNonNull(base64EncodedToken, "Parameter mustn't null");
        final String[] jsonWebTokensParts = base64EncodedToken.split("\\.");
        if (jsonWebTokensParts.length >= 2) {
            return jsonWebTokensParts[1];
        }
        throw new IllegalArgumentException("Parameter is not a valid json web token!");
    }

    public static JsonObject getPayloadAsJsonObject(String base64EncodedToken) {
        String payload = getPayload(base64EncodedToken);
        return Json.createReader(new StringReader(payload)).readObject();
    }

    public static String getUserName(String base64EncodedToken) {
        String decodedPayload = getPayload(base64EncodedToken);
        final JsonObject json = Json.createReader(new StringReader(decodedPayload)).readObject();
        return json.getString("preferred_username");
    }

    public static boolean isTokenExpired(String base64EncodedToken) {
        try {
            final JsonObject payload = getPayloadAsJsonObject(base64EncodedToken);

            // exp: epoch seconds defines the time when the token is expired
            // nbf: epoch seconds defines the time from which the token is valid (the opposite of exp)
            final long expiration = payload.getInt("exp", -1);
            final long notBefore = payload.getInt("nbf", -1);

            long currentTimeInSeconds = Instant.now().getEpochSecond();
            if ((currentTimeInSeconds < expiration || expiration == 0) && (currentTimeInSeconds >= notBefore || notBefore == 0)) {
                return false;
            }
        } catch (Exception ignore) {
        }
        return true;
    }

    public static long getExpirationTime(final String base64EncodedToken) {
        final JsonObject payload = getPayloadAsJsonObject(base64EncodedToken);
        return payload.getInt("exp", -1);
    }

    static boolean isValidTokenObject(final JsonObject entity) {
        return entity != null
                && entity.getJsonString("access_token") != null
                && entity.getJsonString("refresh_token") != null;
    }

    private static Long getJsonNumberFromPayload(final String base64EncodedToken, final String key) {
        final String decodedPayload = getPayload(base64EncodedToken);
        final JsonObject json = Json.createReader(new StringReader(decodedPayload)).readObject();
        final JsonNumber id = json.getJsonNumber(key);
        return id != null ? id.longValue() : null;
    }
}
