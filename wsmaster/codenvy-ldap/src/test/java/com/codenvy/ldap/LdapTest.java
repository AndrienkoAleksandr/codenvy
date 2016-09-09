/*
 *  [2012] - [2016] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.ldap;

import org.ldaptive.BindConnectionInitializer;
import org.ldaptive.Connection;
import org.ldaptive.ConnectionConfig;
import org.ldaptive.Credential;
import org.ldaptive.DefaultConnectionFactory;
import org.ldaptive.Response;
import org.ldaptive.SearchOperation;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchResult;
import org.ldaptive.control.PagedResultsControl;
import org.testng.annotations.Test;

/**
 * Module for testing JPA DAO.
 *
 * @author Mihail Kuznyetsov
 */
public class LdapTest {

    @Test
    public void testName() throws Exception {

        ConnectionConfig conf = new ConnectionConfig("ldap://localhost:32839");
        conf.setConnectionInitializer(new BindConnectionInitializer("cn=admin,dc=willeke,dc=com", new Credential("ldappassword")));

        try (Connection conn = DefaultConnectionFactory.getConnection(conf);) {
            conn.open();
            SearchOperation search = new SearchOperation(conn);
            SearchRequest request = new SearchRequest("dc=willeke,dc=com", "(ou=Product Testing)", "cn", "sn");
            PagedResultsControl prc = new PagedResultsControl(25); // return 25 entries at a time
            request.setControls(prc);
            SearchResult result = new SearchResult();
            byte[] cookie = null;
            do {
                prc.setCookie(cookie);
                Response<SearchResult> response = search.execute(request);
                response.getResult().getEntries().stream().forEach(System.out::println);

                cookie = null;
                PagedResultsControl ctl = (PagedResultsControl) response.getControl(PagedResultsControl.OID);
                if (ctl != null) {
                    if (ctl.getCookie() != null && ctl.getCookie().length > 0) {
                        cookie = ctl.getCookie();
                    }
                }
            } while (cookie != null);
        }

    }

    @Test
    public void test() {
    }
}
