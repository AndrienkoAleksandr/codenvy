package com.codenvy.ldap.sync;

import com.google.common.collect.ImmutableMap;

import org.eclipse.che.api.user.server.model.impl.ProfileImpl;
import org.eclipse.che.commons.lang.Pair;
import org.ldaptive.LdapEntry;

import java.util.Arrays;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

/**
 * @author Yevhenii Voevodin
 */
public class ProfileMapper implements Function<LdapEntry, ProfileImpl> {

    private final ImmutableMap<String, String> attributes;

    public ProfileMapper(Pair<String, String>[] attributes) {
        this.attributes = ImmutableMap.copyOf(Arrays.stream(attributes).collect(toMap(p -> p.first, p -> p.second)));
    }

    @Override
    public ProfileImpl apply(LdapEntry entry) {
        return null;
    }
}