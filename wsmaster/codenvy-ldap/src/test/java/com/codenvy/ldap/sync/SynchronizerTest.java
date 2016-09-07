/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.ldap.sync;

import org.ldaptive.Connection;
import org.ldaptive.Response;
import org.ldaptive.SearchFilter;
import org.ldaptive.SearchOperation;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchResult;
import org.ldaptive.SearchScope;
import org.testng.annotations.Test;

/**
 * @author Yevhenii Voevodin
 */
public class SynchronizerTest {

    private final LdapConnectionFactoryProvider p = new LdapConnectionFactoryProvider("cn=admin,dc=willeke,dc=com",
                                                                                      "ldappassword",
                                                                                      "ldap://172.17.0.2:389",
                                                                                      30_000,
                                                                                      120_000);

    @Test
    public void testSync() throws Exception {
        final Synchronizer synchronizer = new Synchronizer(p.get());

        try (Connection connection = p.get().getConnection()) {
            connection.open();
            final SearchRequest req = new SearchRequest();
            req.setBaseDn("cn=Abby Hermes,ou=Product Development,dc=willeke,dc=com");
            req.setSearchFilter(new SearchFilter("(objectClass=*)"));
            req.setSearchScope(SearchScope.OBJECT);
            req.setReturnAttributes("mail");
            final Response<SearchResult> resp = new SearchOperation(connection).execute(req);
            System.out.println(resp.getResult().getEntry());
        }
    }
}
