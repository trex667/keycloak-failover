package org.schreibvehler.jmeter;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;

public class LoginRefreshSamplerClient extends AbstractJavaSamplerClient {
    private String host;
    private String username;
    private String password;

    Authenticator authenticator;

    @Override
    public Arguments getDefaultParameters() {
        Arguments args = new Arguments();
        args.addArgument("host", "localhost:8080");
        args.addArgument("username", "test-1");
        args.addArgument("password", "t");
        return args;
    }

    @Override
    public void setupTest(JavaSamplerContext context) {
        host = context.getParameter("host");
        username = context.getParameter("username");
        password = context.getParameter("password");
        KeycloakRestRepository repo = new KeycloakRestRepository(host);
        authenticator = new Authenticator(repo);
    }

    @Override
    public SampleResult runTest(JavaSamplerContext javaSamplerContext) {
        SampleResult result = new SampleResult();
        result.sampleStart();
        try {
            if (!authenticator.isTokenBoxInitialized()) {
                authenticator.login(username, password);
                result.setSuccessful(true);
                result.setResponseMessage(String.format("Login of user '%s' successful.", username));
            } else {
                authenticator.refresh();
                result.setSuccessful(true);
                result.setResponseMessage(String.format("Refreshing tokens of user '%s' successful.", username));
            }
        } catch (Exception e) {
            result.setSuccessful(false);
            if (!authenticator.isTokenBoxInitialized()) {
                result.setResponseMessage(String.format("Login of user '%s' failed. StackTrace: %s", username, ExceptionUtils.getStackTrace(e)));
            } else {
                result.setResponseMessage(String.format("Refreshing tokens of user '%s' failed. StackTrace: %s", username, ExceptionUtils.getStackTrace(e)));

            }
        }
        result.sampleEnd();
        return result;
    }

    @Override
    public void teardownTest(JavaSamplerContext context) {
        if (authenticator.isTokenBoxInitialized()) {
            authenticator.logout();
        }
    }
}
