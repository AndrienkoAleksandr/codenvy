package com.codenvy.ldap.sync;

import com.google.common.collect.ImmutableMap;

import org.eclipse.che.api.core.model.user.Profile;
import org.eclipse.che.api.core.model.user.User;
import org.eclipse.che.api.user.server.model.impl.ProfileImpl;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.lang.Pair;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.LdapEntry;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

/**
 * Synchronizes ldap attributes with a custom storage.
 *
 * @author Yevhenii Voevodin
 */
@Singleton
public class Synchronizer {

    private final Long syncPeriodMs;
    private final Long pageSize;
    private final Long pageReadTimeoutMs;

    private final String baseDn;
    private final String userFilter;
    private final String additionalUserDn;

    private final String groupFilter;
    private final String additionalGroupDn;
    private final String membersAttrName;

    private final ConnectionFactory connFactory;
    private final ProfileMapper     profileMapper;
    private final UserMapper        userMapper;

    @Inject
    public Synchronizer(ConnectionFactory connFactory,
                        @Named("ldap.base_dn") String baseDn,
                        @Named("ldap.user.filter") String userFilter,
                        @Named("ldap.user.additional_dn") @Nullable String additionalUserDn,
                        @Named("ldap.group.filter") String groupFilter,
                        @Named("ldap.group.additional_dn") String additionalGroupDn,
                        @Named("ldap.group.attr.members") String membersAttrName,
                        @Named("ldap.sync.period_ms") Long syncPeriodMs,
                        @Named("ldap.sync.page.size") Long pageSize,
                        @Named("ldap.sync.page.read_timeout_ms") Long pageReadTimeoutMs,
                        @Named("ldap.sync.user.attr.id") String userIdAttrName,
                        @Named("ldap.sync.user.attr.name") String userNameAttrName,
                        @Named("ldap.sync.user.attr.email") String userEmailAttrName,
                        @Named("ldap.sync.profile.attrs") Pair<String, String>[] profileAttrs) {
        this.connFactory = connFactory;
        this.baseDn = baseDn;
        this.userFilter = userFilter;
        this.additionalUserDn = additionalUserDn;
        this.groupFilter = groupFilter;
        this.additionalGroupDn = additionalGroupDn;
        this.membersAttrName = membersAttrName;
        this.syncPeriodMs = syncPeriodMs;
        this.pageSize = pageSize;
        this.pageReadTimeoutMs = pageReadTimeoutMs;
        this.userMapper = new UserMapper(userIdAttrName, userNameAttrName, userEmailAttrName);
        this.profileMapper = new ProfileMapper(profileAttrs);
    }

    private static class UserMapper implements Function<LdapEntry, UserImpl> {

        private final String idAttr;
        private final String nameAttr;
        private final String mailAttr;

        private UserMapper(String idAttr, String nameAttr, String emailAttr) {
            this.idAttr = idAttr;
            this.nameAttr = nameAttr;
            this.mailAttr = emailAttr;
        }

        @Override
        public UserImpl apply(LdapEntry entry) {
            return null;
        }
    }

    private static class ProfileMapper implements Function<LdapEntry, ProfileImpl> {

        private final ImmutableMap<String, String> attributes;

        private ProfileMapper(Pair<String, String>[] attrs) {
            attributes = ImmutableMap.copyOf(Arrays.stream(attrs).collect(toMap(pair -> pair.first, pair -> pair.second)));
        }

        @Override
        public ProfileImpl apply(LdapEntry entry) {
            return null;
        }
    }
}
