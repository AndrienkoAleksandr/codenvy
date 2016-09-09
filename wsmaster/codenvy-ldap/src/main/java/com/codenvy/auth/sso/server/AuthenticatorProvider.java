/*
 *  [2012] - [2016] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.auth.sso.server;

import org.ldaptive.auth.Authenticator;
import org.ldaptive.auth.FormatDnResolver;
import org.ldaptive.auth.PooledBindAuthenticationHandler;
import org.ldaptive.auth.PooledCompareAuthenticationHandler;
import org.ldaptive.auth.PooledSearchDnResolver;

import javax.inject.Provider;
import java.beans.Beans;

/**
 * Created by sj on 08.09.16.
 */
public class AuthenticatorProvider implements Provider<Authenticator> {
    @Override
    public Authenticator get() {
        return null;
    }

    private static Authenticator getAuthenticator(final LdapAuthenticationConfiguration l) {
        switch (l.getType()){
            case AD:
                return getActiveDirectoryAuthenticator(l);
                break;
            case DIRECT:
                return getDirectBindAuthenticator(l);
            break;
            case SASL:
                return getSaslAuthenticator(l);
            break;
            default:
                return getAuthenticatedOrAnonSearchAuthenticator(l);
               break;
        }
        
    }

    private static Authenticator getSaslAuthenticator(final LdapAuthenticationConfiguration l) {
        final PooledSearchDnResolver resolver = new PooledSearchDnResolver();
        resolver.setBaseDn(l.getBaseDn());
        resolver.setSubtreeSearch(l.isSubtreeSearch());
        resolver.setAllowMultipleDns(l.isAllowMultipleDns());
        resolver.setConnectionFactory(Beans.newPooledConnectionFactory(l));
        resolver.setUserFilter(l.getUserFilter());
        return new Authenticator(resolver, getPooledBindAuthenticationHandler(l));
    }

    private static Authenticator getAuthenticatedOrAnonSearchAuthenticator(final LdapAuthenticationConfiguration l) {
        final PooledSearchDnResolver resolver = new PooledSearchDnResolver();
        resolver.setBaseDn(l.getBaseDn());
        resolver.setSubtreeSearch(l.isSubtreeSearch());
        resolver.setAllowMultipleDns(l.isAllowMultipleDns());
        resolver.setConnectionFactory(Beans.newPooledConnectionFactory(l));
        resolver.setUserFilter(l.getUserFilter());

        final Authenticator auth;
        if (StringUtils.isBlank(l.getPrincipalAttributePassword())) {
            auth = new Authenticator(resolver, getPooledBindAuthenticationHandler(l));
        } else {
            auth = new Authenticator(resolver, getPooledCompareAuthenticationHandler(l));
        }
        auth.setEntryResolver(Beans.newSearchEntryResolver(l));

        return auth;
    }

    private static Authenticator getDirectBindAuthenticator(final LdapAuthenticationConfiguration l) {
        final FormatDnResolver resolver = new FormatDnResolver(l.getBaseDn());
        return new Authenticator(resolver, getPooledBindAuthenticationHandler(l));
    }

    private static Authenticator getActiveDirectoryAuthenticator(final LdapAuthenticationConfiguration l) {
        final FormatDnResolver resolver = new FormatDnResolver(l.getDnFormat());
        final Authenticator authn = new Authenticator(resolver, getPooledBindAuthenticationHandler(l));
        authn.setEntryResolver(Beans.newSearchEntryResolver(l));
        return authn;
    }

    private static PooledBindAuthenticationHandler getPooledBindAuthenticationHandler(final LdapAuthenticationConfiguration l) {
        final PooledBindAuthenticationHandler handler = new PooledBindAuthenticationHandler(Beans.newPooledConnectionFactory(l));
        handler.setAuthenticationControls(new PasswordPolicyControl());
        return handler;
    }

    private static PooledCompareAuthenticationHandler getPooledCompareAuthenticationHandler(final LdapAuthenticationConfiguration l) {
        final PooledCompareAuthenticationHandler handler = new PooledCompareAuthenticationHandler(
                Beans.newPooledConnectionFactory(l));
        handler.setPasswordAttribute(l.getPrincipalAttributePassword());
        return handler;
    }
}
