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

import org.apache.commons.io.FileUtils;
import org.apache.commons.compress.utils.IOUtils;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.rest.Service;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;

/**
 * Defines Audit report REST API.
 *
 * @author Igor Vinokur
 */
@Path("/audit")
public class AuditService extends Service {

    private static final String GENERATE_AUDIT_REPORT_ERROR = "Failed to generate report audit";

    private final AdminUserDao          adminUserDao;
    private final WorkspaceManager      workspaceManager;
    private final PermissionManager     permissionManager;
    private final CodenvyLicenseManager licenseManager;

    @Inject
    public AuditService(AdminUserDao adminUserDao,
                        WorkspaceManager workspaceManager,
                        PermissionManager permissionManager,
                        CodenvyLicenseManager licenseManager) {
        this.adminUserDao = adminUserDao;
        this.workspaceManager = workspaceManager;
        this.permissionManager = permissionManager;
        this.licenseManager = licenseManager;
    }

    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public StreamingOutput getReport() throws ServerException, NotFoundException {
        final CodenvyLicense license = licenseManager.load();
        final File tempDir = Files.createTempDir();
        final File file = new File(tempDir, "report.txt");
        appendToFile("Number of all users: " + adminUserDao.getAll(1, 0).getTotalItemsCount() + "\n", file);
        appendToFile("Number of users licensed: " + license.getNumberOfUsers() + "\n", file);
        appendToFile("Date when license expires: " + new SimpleDateFormat("dd MMMM yyyy").format(license.getExpirationDate()) + "\n", file);
        int skipItems = 0;
        while (true) {
            Page<UserImpl> page = adminUserDao.getAll(20, skipItems);
            List<UserImpl> users = page.getItems();
            if (users.size() == 0) {
                break;
            } else {
                skipItems += users.size();
            }
            for (UserImpl user : users) {
                List<WorkspaceImpl> workspaces = workspaceManager.getWorkspaces(user.getId());
                int workspacesNumber = workspaces.size();
                long ownWorkspacesNumber = workspaces.stream().filter(workspace -> workspace.getNamespace().equals(user.getName())).count();
                String userRow = user.getEmail() + " is owner of " +
                                 ownWorkspacesNumber + " workspace" + (ownWorkspacesNumber > 1 | ownWorkspacesNumber == 0 ? "s" : "") +
                                 " and has permissions in " + workspacesNumber + " workspace" +
                                 (workspacesNumber > 1 | workspacesNumber == 0 ? "s" : "") + "\n";
                appendToFile(userRow, file);
                String userId = user.getId();
                for (WorkspaceImpl workspace : workspaces) {
                    String wsRow = "   └ " + workspace.getConfig().getName() +
                          " owner: " + workspace.getNamespace().equals(user.getName()) +
                          " permissions: " + getWorkspacePermissions(workspace.getId(), userId).getActions().toString() + "\n";
                    appendToFile(wsRow, file);
                }
            }
        }
        return output -> {
            BufferedOutputStream bus = new BufferedOutputStream(output);
            bus.write(IOUtils.toByteArray(new FileInputStream(file)));
            FileUtils.deleteDirectory(tempDir);
        };
    }

    private void appendToFile(String row, File file) throws ServerException {
        try {
            Files.append(row, file, Charset.defaultCharset());
        } catch (IOException e) {
            throw new ServerException(GENERATE_AUDIT_REPORT_ERROR, e);
        }
    }

    private PermissionsImpl getWorkspacePermissions(String workspaceId, String userId) throws NotFoundException, ServerException {
        List<PermissionsImpl> permissions;
        try {
            permissions = permissionManager.getByInstance("workspace", workspaceId);
        } catch (ConflictException exception) {
            throw new ServerException(GENERATE_AUDIT_REPORT_ERROR, exception);
        }
        Optional<PermissionsImpl> optional = permissions.stream()
                                                        .filter(wsPermissions -> wsPermissions.getUser().equals(userId))
                                                        .findFirst();
        if (optional.isPresent()) {
            return optional.get();
        } else {
            throw new NotFoundException("Permissions for user " + userId + " in workspace " + workspaceId + " was not found");
        }
    }
}
