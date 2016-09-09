package com.codenvy.ldap.sync;

import com.codenvy.ldap.sync.Synchronizer.LdapGroupsConfig;

import org.ldaptive.Connection;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapException;

/**
 * @author Yevhenii Voevodin
 */
public class MembershipSelector implements LdapEntrySelector {

    private final LdapGroupsConfig groupsConfig;

    public MembershipSelector(LdapGroupsConfig groupsConfig) {
        this.groupsConfig = groupsConfig;
    }

    @Override
    public Iterable<LdapEntry> select(Connection connection) throws LdapException {
        return null;
    }
}
