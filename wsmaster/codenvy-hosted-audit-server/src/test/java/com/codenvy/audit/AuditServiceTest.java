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
package com.codenvy.audit;

import com.codenvy.api.license.server.license.CodenvyLicense;
import com.codenvy.api.license.server.license.CodenvyLicenseManager;
import com.codenvy.api.permission.server.PermissionManager;
import com.codenvy.api.permission.server.PermissionsImpl;
import com.codenvy.api.user.server.dao.AdminUserDao;
import com.google.common.io.Files;
import com.jayway.restassured.response.Response;

import org.apache.commons.io.FileUtils;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.everrest.assured.EverrestJetty;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.jayway.restassured.RestAssured.given;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_NAME;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_PASSWORD;
import static org.everrest.assured.JettyHttpServer.SECURE_PATH;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * Tests for {@link AuditService}.
 *
 * @author Igor Vinokur
 */
@Listeners(value = {EverrestJetty.class, MockitoTestNGListener.class})
public class AuditServiceTest {

    @Mock
    private AdminUserDao      adminUserDao;
    @Mock
    private WorkspaceManager  workspaceManager;
    @Mock
    private PermissionManager permissionManager;
    @Mock
    private CodenvyLicenseManager licenseManager;
    @InjectMocks
    private AuditService      service;

    @Test
    public void shouldReturnAuditReport() throws Exception {
        CodenvyLicense codenvyLicense = mock(CodenvyLicense.class);
        when(licenseManager.load()).thenReturn(codenvyLicense);
        when(codenvyLicense.getNumberOfUsers()).thenReturn(15);
        when(codenvyLicense.getExpirationDate()).thenReturn(new SimpleDateFormat("dd MMMM yyyy").parse("01 January 2016"));

        UserImpl user = mock(UserImpl.class);
        when(user.getEmail()).thenReturn("user@email.com");
        when(user.getId()).thenReturn("UserId");
        when(user.getName()).thenReturn("User");
        UserImpl otherUser = mock(UserImpl.class);
        when(otherUser.getEmail()).thenReturn("otherUser@email.com");
        when(otherUser.getId()).thenReturn("OtherUserId");
        when(otherUser.getName()).thenReturn("OtherUser");

        WorkspaceImpl workspace1 = mock(WorkspaceImpl.class);
        WorkspaceImpl workspace2 = mock(WorkspaceImpl.class);
        WorkspaceImpl workspace3 = mock(WorkspaceImpl.class);
        when(workspace1.getNamespace()).thenReturn("User");
        when(workspace2.getNamespace()).thenReturn("OtherUser");
        when(workspace3.getNamespace()).thenReturn("OtherUser");
        when(workspace1.getId()).thenReturn("Workspace1Id");
        when(workspace2.getId()).thenReturn("Workspace2Id");
        when(workspace3.getId()).thenReturn("Workspace3Id");

        WorkspaceConfigImpl ws1config = mock(WorkspaceConfigImpl.class);
        when(ws1config.getName()).thenReturn("Workspace1Name");
        WorkspaceConfigImpl ws2config = mock(WorkspaceConfigImpl.class);
        when(ws2config.getName()).thenReturn("Workspace2Name");
        WorkspaceConfigImpl ws3config = mock(WorkspaceConfigImpl.class);
        when(ws3config.getName()).thenReturn("Workspace3Name");
        when(workspace1.getConfig()).thenReturn(ws1config);
        when(workspace2.getConfig()).thenReturn(ws2config);
        when(workspace3.getConfig()).thenReturn(ws3config);

        PermissionsImpl ws1UserPermissions = mock(PermissionsImpl.class);
        PermissionsImpl ws2UserPermissions = mock(PermissionsImpl.class);
        PermissionsImpl ws2OtherUserPermissions = mock(PermissionsImpl.class);
        PermissionsImpl ws3OtherUserPermissions = mock(PermissionsImpl.class);
        when(ws1UserPermissions.getUser()).thenReturn("UserId");
        when(ws1UserPermissions.getActions()).thenReturn(asList("run", "edit", "delete"));
        when(ws2UserPermissions.getUser()).thenReturn("UserId");
        when(ws2OtherUserPermissions.getUser()).thenReturn("OtherUserId");
        when(ws3OtherUserPermissions.getUser()).thenReturn("OtherUserId");
        when(permissionManager.getByInstance(anyString(), eq("Workspace1Id"))).thenReturn(singletonList(ws1UserPermissions));
        when(permissionManager.getByInstance(anyString(), eq("Workspace2Id"))).thenReturn(asList(ws2UserPermissions, ws2OtherUserPermissions));
        when(permissionManager.getByInstance(anyString(), eq("Workspace2Id"))).thenReturn(asList(ws2UserPermissions, ws2OtherUserPermissions));
        when(permissionManager.getByInstance(anyString(), eq("Workspace3Id"))).thenReturn(singletonList(ws3OtherUserPermissions));


        Page page = mock(Page.class);
        Page emptyPage = mock(Page.class);
        when(page.getItems()).thenReturn(asList(user, otherUser));
        when(page.getTotalItemsCount()).thenReturn(2L);
        when(emptyPage.getItems()).thenReturn(emptyList());
        when(workspaceManager.getWorkspaces("UserId")).thenReturn(asList(workspace1, workspace2));
        when(workspaceManager.getWorkspaces("OtherUserId")).thenReturn(singletonList(workspace3));
        when(adminUserDao.getAll(1, 0)).thenReturn(page);
        when(adminUserDao.getAll(20, 0)).thenReturn(page);
        when(adminUserDao.getAll(20, 2)).thenReturn(emptyPage);

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .when()
                                         .get(SECURE_PATH + "/audit");

        assertEquals(response.getStatusCode(), 200);

        FileUtils.copyInputStreamToFile(response.getBody().asInputStream(), new File(Files.createTempDir(), "report.txt"));
    }
}
