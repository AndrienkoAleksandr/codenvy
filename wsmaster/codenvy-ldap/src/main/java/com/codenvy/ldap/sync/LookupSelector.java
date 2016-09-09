package com.codenvy.ldap.sync;

import org.ldaptive.Connection;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapException;
import org.ldaptive.Response;
import org.ldaptive.SearchFilter;
import org.ldaptive.SearchOperation;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchResult;
import org.ldaptive.SearchScope;
import org.ldaptive.control.util.PagedResultsClient;

import java.util.ArrayList;

import static org.ldaptive.ResultCode.SUCCESS;

// TODO add paging

/**
 * @author Yevhenii Voevodin
 */
public class LookupSelector implements LdapEntrySelector {

    private final String   filter;
    private final String   baseDn;
    private final String[] attributes;
    private final int      pageSize;

    public LookupSelector(int pageSize,
                          String baseDn,
                          String filter,
                          String[] attributes) {
        this.filter = filter;
        this.baseDn = baseDn;
        this.attributes = attributes;
        this.pageSize = pageSize;
    }

    @Override
    public Iterable<LdapEntry> select(Connection connection) throws LdapException {
        final SearchRequest req = new SearchRequest();
        req.setBaseDn(baseDn);
        req.setSearchFilter(new SearchFilter(filter));
        req.setReturnAttributes(attributes);
        req.setSizeLimit(pageSize);
        req.setSearchScope(SearchScope.SUBTREE);
        final PagedResultsClient prClient = new PagedResultsClient(connection, pageSize);
        final Response<SearchResult> response = prClient.execute(req);
        if (response.getResultCode() != SUCCESS) {
            throw new LdapException("Couldn't retrieve ldap entries", response.getResultCode());
        }
        return response.getResult().getEntries();
    }
}
