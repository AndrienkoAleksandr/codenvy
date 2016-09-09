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

import org.apache.directory.server.core.DefaultDirectoryService;
import org.apache.directory.shared.ldap.entry.ServerEntry;
import org.ldaptive.Connection;
import org.ldaptive.ConnectionFactory;
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

import static org.testng.Assert.assertEquals;

/**
 * Tests {@link Synchronizer}.
 *
 * @author Yevhenii Voevodin
 */
public class SynchronizerTest {

    private static final String BASE_DN = "dc=codenvy,dc=com";

    private MyLdapServer      server;
    private ConnectionFactory connFactory;

    @BeforeMethod
    public void startServer() throws Exception {
        server = MyLdapServer.builder()
                             .setPartitionId("codenvy")
                             .setPartitionDn(BASE_DN)
                             .useTmpWorkingDir()
                             .build();
        server.start();

        connFactory = new LdapConnectionFactoryProvider(server.getAdminDn(),
                                                        server.getAdminPassword(),
                                                        server.getUrl(),
                                                        30_000,
                                                        120_000).get();
    }

    @AfterMethod
    public void stopServer() throws Exception {
        server.stop();
    }

    @Test
    public void test() throws Exception {
        try (Connection conn = connFactory.getConnection()) {
            conn.open();
            final SearchRequest req = new SearchRequest();
            req.setBaseDn(BASE_DN);
            req.setSearchFilter(new SearchFilter("(objectClass=*)"));
            req.setSearchScope(SearchScope.SUBTREE);
            final Response<SearchResult> resp = new SearchOperation(conn).execute(req);
            assertEquals(resp.getResult().getEntries().size(), 2);
        }
    }
}
