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
package com.codenvy.im.cli.command;

import org.apache.karaf.shell.commands.Command;
import org.eclipse.che.api.user.shared.dto.UserDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;

import java.util.List;

/**
 * @author Igor Vinokur
 */


@Command(scope = "codenvy", name = "audit", description = "Print the list of available latest versions and installed ones")
public class AuditCommand extends AbstractIMCommand {

    @Override
    protected void doExecuteCommand() throws Exception {

        List<UserDto> allUsers = facade.getAuditReport();

        for(UserDto user : allUsers) {
            List<WorkspaceDto> workspaces = facade.getWorkspaces(user.getId());
        }

        console.println(allUsers.toString());
    }
}
