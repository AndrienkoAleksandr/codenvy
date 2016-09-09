package com.codenvy.ldap.sync;

import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.ldaptive.LdapEntry;

import java.util.function.Function;

/**
 * @author Yevhenii Voevodin
 */
public class UserMapper implements Function<LdapEntry, UserImpl> {

    private final String idAttr;
    private final String nameAttr;
    private final String mailAttr;

    public UserMapper(String idAttr, String nameAttr, String emailAttr) {
        this.idAttr = idAttr;
        this.nameAttr = nameAttr;
        this.mailAttr = emailAttr;
    }

    @Override
    public UserImpl apply(LdapEntry entry) {
        return null;
    }
}
