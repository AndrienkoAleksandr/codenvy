package com.codenvy.ldap.sync;


import org.ldaptive.ConnectionFactory;

import javax.inject.Inject;

/**
 * Synchronizes ldap attributes with a custom storage.
 *
 * @author Yevhenii Voevodin
 */
public class Synchronizer {

    private final ConnectionFactory connFactory;

    @Inject
    public Synchronizer(ConnectionFactory connFactory) {
        this.connFactory = connFactory;
    }
}
