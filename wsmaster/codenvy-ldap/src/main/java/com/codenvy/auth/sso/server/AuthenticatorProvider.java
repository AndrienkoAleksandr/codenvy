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
