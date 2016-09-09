package com.codenvy.auth.sso.server;


import com.codenvy.api.dao.authentication.AuthenticationHandler;

import org.eclipse.che.api.auth.AuthenticationException;
import org.ldaptive.LdapException;
import org.ldaptive.auth.AuthenticationRequest;
import org.ldaptive.auth.AuthenticationResponse;
import org.ldaptive.auth.AuthenticationResultCode;
import org.ldaptive.auth.Authenticator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

/**
 * Created by sj on 08.09.16.
 */
public class LdapAuthentificationHandler implements AuthenticationHandler {

    private static final Logger LOG = LoggerFactory.getLogger(LdapAuthentificationHandler.class);

    private final Authenticator ldapAuthenticator;

    @Inject
    public LdapAuthentificationHandler(Authenticator ldapAuthenticator) {
        this.ldapAuthenticator = ldapAuthenticator;
    }


    @Override
    public void authenticate(String login, String password) throws AuthenticationException {

        final AuthenticationResponse response;
        try {
            LOG.debug("Attempting LDAP authentication for: {}", login);
            final AuthenticationRequest request = new AuthenticationRequest(login,
                                                                            new org.ldaptive.Credential(password));
            response = this.ldapAuthenticator.authenticate(request);
        } catch (final LdapException e) {
            throw new AuthenticationException(401, "Unexpected LDAP error");
        }
        LOG.debug("LDAP response: {}", response);

        if (!response.getResult()) {
            throw new AuthenticationException(401, "Authentication failed. Please check username and password.");
        }

        if (AuthenticationResultCode.DN_RESOLUTION_FAILURE == response.getAuthenticationResultCode()) {
            throw new AuthenticationException(login + "  is not found");
        }
    }

    @Override
    public String getType() {
        return "ldap";
    }
}
