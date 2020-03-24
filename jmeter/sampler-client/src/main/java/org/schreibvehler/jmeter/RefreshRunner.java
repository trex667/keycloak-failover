package org.schreibvehler.jmeter;

public class RefreshRunner implements Runnable {
    private final Authenticator authenticator;
    private final String username;
    private final String password;

    public RefreshRunner(KeycloakRestRepository repo, String username, String password) {
        this.authenticator = new Authenticator(repo);
        this.username = username;
        this.password = password;
    }

    @Override
    public void run() {
        if (!authenticator.isTokenBoxInitialized()) {
            authenticator.login(username, password);
        } else {
            authenticator.refresh();
        }
    }

    public void logout() {
        if (authenticator.isTokenBoxInitialized()) {
            authenticator.logout();
        }
    }
}
