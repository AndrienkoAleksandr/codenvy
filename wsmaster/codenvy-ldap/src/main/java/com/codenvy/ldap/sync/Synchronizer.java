package com.codenvy.ldap.sync;


import org.ldaptive.ConnectionFactory;

import javax.inject.Inject;
import javax.inject.Named;
/**
 * Synchronizes ldap attributes with a custom storage.
 *
 * @author Yevhenii Voevodin
 */
@SuppressWarnings("unused")
public class Synchronizer {

    private final ConnectionFactory connFactory;

    @Inject
    public Synchronizer(ConnectionFactory connFactory) {
        this.connFactory = connFactory;
    }

    public void sync() {

    }
}
