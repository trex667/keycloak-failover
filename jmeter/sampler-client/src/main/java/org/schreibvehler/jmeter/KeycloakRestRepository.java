package org.schreibvehler.jmeter;

import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.StringReader;
import java.net.URI;
import java.util.concurrent.TimeUnit;

public class KeycloakRestRepository {

    private static final String REALM = "test";
    private static final String CLIENT_ID = "admin-cli";
    private final Client client;
    private final URI uri;

    public KeycloakRestRepository(String hostname) {
        this.client = new ResteasyClientBuilder()
                .establishConnectionTimeout(100, TimeUnit.SECONDS)
                .socketTimeout(10, TimeUnit.SECONDS)
                .build();

        this.uri = URI.create("http://" + hostname);
    }

    private static JsonObject fromString(String json) {
        try (JsonReader jsonReader = Json.createReader(new StringReader(json))) {
            return jsonReader.readObject();
        }
    }

    public void close() {
        client.close();
    }

    public JsonObject authenticate(User user) {
        WebTarget target = client.target(uri)
                .path(getTokenPath());
        Response response = target
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(getUserPasswordRequestParameter(user), MediaType.APPLICATION_FORM_URLENCODED));
        try {
            if (response.getStatus() != 200) {
                throw new WebApplicationException(response);
            }
            return fromString(response.readEntity(String.class));
        } finally {
            response.close();
        }
    }

    private Form getUserPasswordRequestParameter(User user) {
        final Form result = new Form();
        result.param("client_id", CLIENT_ID);
        result.param("grant_type", "password");
        result.param("username", user.getName());
        result.param("password", user.getPassword());
        return result;
    }

    private String getTokenPath() {
        return String.format("/auth/realms/%s/protocol/openid-connect/token", REALM);
    }

    public JsonObject refresh(TokenBox tokenBox) {
        WebTarget target = client.target(uri)
                .path(getTokenPath());
        Response response = target
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(getRefreshRequestParameter(tokenBox), MediaType.APPLICATION_FORM_URLENCODED));
        try {
            if (response.getStatus() != 200) {
                throw new WebApplicationException(response);
            }
            return fromString(response.readEntity(String.class));
        } finally {
            response.close();
        }
    }

    private Form getRefreshRequestParameter(TokenBox tokenBox) {
        final Form result = new Form();
        result.param("client_id", CLIENT_ID);
        result.param("grant_type", "refresh_token");
        result.param("refresh_token", tokenBox.getRefreshToken());
        return result;
    }

    public void logout(TokenBox tokenBox) {
        WebTarget target = client.target(uri)
                .path(getLogoutPath());
        Response response = target
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(getLogoutRequestParameter(tokenBox), MediaType.APPLICATION_FORM_URLENCODED));
        try {
            if (response.getStatus() != 204) {
                throw new WebApplicationException(response);
            }
        } finally {
            response.close();
        }
    }

    private Form getLogoutRequestParameter(TokenBox tokenBox) {
        final Form result = new Form();
        result.param("client_id", CLIENT_ID);
        result.param("refresh_token", tokenBox.getRefreshToken());
        return result;
    }

    private String getLogoutPath() {
        return String.format("/auth/realms/%s/protocol/openid-connect/logout", REALM);
    }
}
