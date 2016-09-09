package com.codenvy.ldap.sync;

import org.eclipse.che.api.user.server.model.impl.ProfileImpl;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.lang.Pair;
import org.ldaptive.Connection;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import java.util.ArrayList;
import java.util.function.Function;

import static java.lang.String.format;

/**
 * Synchronizes ldap attributes with a custom storage.
 *
 * @author Yevhenii Voevodin
 */
@Singleton
public class Synchronizer {

    private final Long                             syncPeriodMs;
    private final int                              pageSize;
    private final Long                             pageReadTimeoutMs;
    private final String                           baseDn;
    private final String                           userFilter;
    private final ConnectionFactory                connFactory;
    private final Function<LdapEntry, ProfileImpl> profileMapper;
    private final Function<LdapEntry, UserImpl>    userMapper;
    private final LdapGroupsConfig                 groupsConfig;
    private final LdapEntrySelector                selector;

    @Inject
    public Synchronizer(ConnectionFactory connFactory,
                        LdapGroupsConfig groupsConfig,
                        @Named("ldap.base_dn") String baseDn,
                        @Named("ldap.user.filter") String userFilter,
                        @Named("ldap.user.additional_dn") @Nullable String additionalUserDn,
                        @Named("ldap.sync.period_ms") Long syncPeriodMs,
                        @Named("ldap.sync.page.size") int pageSize,
                        @Named("ldap.sync.page.read_timeout_ms") Long pageReadTimeoutMs,
                        @Named("ldap.sync.user.attr.id") String userIdAttr,
                        @Named("ldap.sync.user.attr.name") String userNameAttr,
                        @Named("ldap.sync.user.attr.email") String userEmailAttr,
                        @Named("ldap.sync.profile.attrs") @Nullable Pair<String, String>[] profileAttributes) {
        this.connFactory = connFactory;
        this.groupsConfig = groupsConfig;
        this.baseDn = baseDn;
        this.userFilter = userFilter;
        this.syncPeriodMs = syncPeriodMs;
        this.pageSize = pageSize;
        this.pageReadTimeoutMs = pageReadTimeoutMs;

        // getting attribute names which should be synchronized
        final ArrayList<String> attrsList = new ArrayList<>();
        attrsList.add(userIdAttr);
        attrsList.add(userNameAttr);
        attrsList.add(userEmailAttr);
        if (profileAttributes != null) {
            for (Pair<String, String> profileAttribute : profileAttributes) {
                attrsList.add(profileAttribute.second);
            }
        }
        final String[] syncAttributes = attrsList.toArray(new String[attrsList.size()]);

        this.userMapper = new UserMapper(userIdAttr, userNameAttr, userEmailAttr);
        this.profileMapper = profileAttributes == null ? null : new ProfileMapper(profileAttributes);

        if (groupsConfig.isEnabled()) {
            selector = new MembershipSelector(groupsConfig);
        } else {
            selector = new LookupSelector(pageSize,
                                          additionalUserDn == null ? baseDn : additionalUserDn + ',' + baseDn,
                                          userFilter,
                                          syncAttributes);
        }
    }

    public void syncAll() throws LdapException {
        try (Connection connection = connFactory.getConnection()) {
            connection.open();
            for (LdapEntry entry : selector.select(connection)) {
                System.out.println(entry);
            }
        }
    }

    /** Defines configuration for ldap groups. */
    static class LdapGroupsConfig {

        private final String groupFilter;
        private final String additionalDn;
        private final String membersAttrName;

        @com.google.inject.Inject(optional = true)
        LdapGroupsConfig(@Named("ldap.group.filter") @Nullable String groupFilter,
                         @Named("ldap.group.additional_dn") @Nullable String additionalGroupDn,
                         @Named("ldap.group.attr.members") @Nullable String membersAttrName) {
            if (groupFilter != null && membersAttrName == null) {
                throw new NullPointerException(format("Value of 'ldap.group.filter' is set to '%s', which means that groups search " +
                                                      "is enabled that also requires 'ldap.group.attr.members' to be set",
                                                      groupFilter));
            }
            this.groupFilter = groupFilter;
            this.additionalDn = additionalGroupDn;
            this.membersAttrName = membersAttrName;
        }

        private boolean isEnabled() {
            return groupFilter != null;
        }
    }
}
