package com.codenvy.ldap.sync;


import org.ldaptive.BindConnectionInitializer;
import org.ldaptive.ConnectionConfig;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.Credential;
import org.ldaptive.DefaultConnectionFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

/**
 * @author Yevhenii Voevodin
 */
@Singleton
public class LdapConnectionFactoryProvider implements Provider<ConnectionFactory> {

    private final ConnectionFactory connFactory;

    @Inject
    public LdapConnectionFactoryProvider(@Named("ldap.sync.principal") String principal,
                                         @Named("ldap.sync.password") String password,
                                         @Named("ldap.sync.url") String url) {
        final ConnectionConfig connConfig = new ConnectionConfig();
        connConfig.setLdapUrl(url);
        connConfig.setConnectionInitializer(new BindConnectionInitializer(principal, new Credential(password)));
        connFactory = new DefaultConnectionFactory(connConfig);
    }

    @Override
    public ConnectionFactory get() {
        return connFactory;
    }
}
