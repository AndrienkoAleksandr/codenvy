package com.codenvy.ldap.sync;

import org.ldaptive.Connection;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapException;

/**
 * @author Yevhenii Voevodin
 */
public interface LdapEntrySelector {

    Iterable<LdapEntry> select(Connection connection) throws LdapException;
}
