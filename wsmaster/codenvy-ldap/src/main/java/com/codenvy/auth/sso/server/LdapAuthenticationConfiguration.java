package com.codenvy.auth.sso.server;

/**
 * Created by sj on 08.09.16.
 */
public class LdapAuthenticationConfiguration {
    /**
     * The enum Authentication types.
     */
    public enum AuthenticationType {
        /**
         * Active Directory.
         */
        AD,
        /**
         * Authenticated Search.
         */
        AUTHENTICATED,
        /**
         * Direct Bind.
         */
        DIRECT,
        /**
         * Anonymous Search.
         */
        ANONYMOUS,
        /**
         * SASL bind search.
         */
        SASL
    }

    public AuthenticationType getType() {
        return null;
    }
}
