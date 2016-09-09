package com.codenvy.auth.sso.server;

import org.ldaptive.auth.EntryResolver;
import org.ldaptive.auth.PooledSearchEntryResolver;
import org.ldaptive.pool.PooledConnectionFactory;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Created by sj on 09.09.16.
 */
public class EntryResolverProvider implements Provider<EntryResolver> {

    private final PooledSearchEntryResolver entryResolver;

    @Inject
    public EntryResolverProvider(PooledConnectionFactory connFactory, LdapConfiguration configuration) {
        this.entryResolver = new PooledSearchEntryResolver();
        this.entryResolver.setBaseDn(configuration.getBaseDn());
        this.entryResolver.setUserFilter(configuration.getUserFilter());
        this.entryResolver.setSubtreeSearch(configuration.isSubtreeSearch());
        this.entryResolver.setConnectionFactory(connFactory);

    }

    @Override
    public EntryResolver get() {
        return entryResolver;
    }
}
