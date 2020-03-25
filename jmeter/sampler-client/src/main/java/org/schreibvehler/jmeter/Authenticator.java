package org.schreibvehler.jmeter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.JsonObject;

public class Authenticator {
    private static final Logger LOG = LoggerFactory.getLogger("jmeter");

    private final KeycloakRestRepository repo;
    private TokenBox tokenBox = null;
    private boolean isAuthenticated = false;
    private int logins = 0;
    private static final int MAX_LOGINS = 1;

    public Authenticator(KeycloakRestRepository repo) {
        this.repo = repo;
    }

    public void login(String username, String password) {
        if (!isAuthenticated && logins < MAX_LOGINS) {
            try {
                JsonObject tokenJson = repo.authenticate(new User(username, password));
                tokenBox = new TokenBox(tokenJson.getString("access_token"), tokenJson.getString("refresh_token"));
                isAuthenticated = true;
                LOG.info(String.format("Login of user '%s' successful.", username));
            } catch (Exception e) {
                throw new AuthException(String.format("Login of user '%s' failed!", username), e);
            }
        } else {
            LOG.info(String.format("Login ignored ===> Login of user %s failed at least for %d times", username, MAX_LOGINS));
        }
    }

    public void refresh() {
        validateTokenBox();
        String username = TokenUtils.getUserName(tokenBox.getAccessToken());
        try {
            if (isAccessTokenExpired(tokenBox)) {
                JsonObject tokenJson = repo.refresh(tokenBox);
                tokenBox = new TokenBox(tokenJson.getString("access_token"), tokenJson.getString("refresh_token"));
                LOG.info(String.format("AccessToken of user '%s' is expired! ====> token refreshed.", username));
            } else {
                LOG.debug(String.format("AccessToken of user '%s' is still valid! Nothing to do.", username));
            }
        } catch (Exception e) {
            throw new AuthException(String.format("Refreshing of tokens for user '%s' failed!", username), e);
        }
    }

    private boolean isAccessTokenExpired(TokenBox tokenBox) {
        return TokenUtils.isTokenExpired(tokenBox.getAccessToken());
    }

    public void logout() {
        validateTokenBox();
        String username = TokenUtils.getUserName(tokenBox.getAccessToken());
        try {
            repo.logout(tokenBox);
            LOG.info(String.format("Logout user '%s' successful.", username));
        } catch (Exception e) {
            throw new AuthException(String.format("Logout of user '%s' failed!", username), e);
        } finally {
            tokenBox = null;
        }

    }

    public boolean isAuthenticated() {
        return isAuthenticated;
    }

    private void validateTokenBox() {
        if (!isAuthenticated) {
            throw new AuthException("Not yet authenticated!");
        }
        if (tokenBox == null || tokenBox.getAccessToken() == null || tokenBox.getRefreshToken() == null) {
            throw new AuthException("Tokenbox is null or accessToken or refreshToken is null!");
        }
    }
}
