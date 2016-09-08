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

import com.codenvy.ldap.LdapConnectionFactoryProvider;
import com.codenvy.ldap.MyLdapServer;

import org.apache.directory.shared.ldap.entry.ServerEntry;
import org.ldaptive.Connection;
import org.ldaptive.DefaultConnectionFactory;
import org.ldaptive.Response;
import org.ldaptive.SearchFilter;
import org.ldaptive.SearchOperation;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchResult;
import org.ldaptive.SearchScope;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;

/**
 * Tests {@link Synchronizer}.
 *
 * @author Yevhenii Voevodin
 */
public class SynchronizerTest {

    private static final String BASE_DN = "dc=codenvy,dc=com";

    private final LdapConnectionFactoryProvider p = new LdapConnectionFactoryProvider("cn=admin,dc=willeke,dc=com",
                                                                                      "ldappassword",
                                                                                      "ldap://172.17.0.2:389",
                                                                                      30_000,
                                                                                      120_000);

    private MyLdapServer server;

    @BeforeMethod
    public void startServer() throws Exception {
        server = MyLdapServer.builder()
                             .setPartitionId("codenvy")
                             .setPartitionDn(BASE_DN)
                             .allowAnonymousAccess()
                             .useTmpWorkingDir()
                             .build();

        final ServerEntry newEntry = server.createEntry("uid", "vova");
        newEntry.add("objectClass", "inetOrgPerson");
        newEntry.add("cn", "vovka");
        newEntry.add("uid", "id");
        newEntry.add("sn", "<none>");
        server.addEntry(newEntry);

        server.start();
    }

    @AfterMethod
    public void stopServer() throws Exception {
        server.stop();
    }

    @Test
    public void test() throws Exception {
        try (Connection connection = DefaultConnectionFactory.getConnection(server.getUrl())) {
            connection.open();
            final SearchRequest req = new SearchRequest();
            req.setBaseDn(BASE_DN);
            req.setSearchFilter(new SearchFilter("(objectClass=*)"));
            req.setSearchScope(SearchScope.SUBTREE);
            final Response<SearchResult> resp = new SearchOperation(connection).execute(req);
            assertFalse(resp.getResult().getEntries().isEmpty());
        }
    }
}
