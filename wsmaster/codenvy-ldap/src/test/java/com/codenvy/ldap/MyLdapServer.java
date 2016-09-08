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
package com.codenvy.ldap;

import org.apache.directory.server.constants.ServerDNConstants;
import org.apache.directory.server.core.CoreSession;
import org.apache.directory.server.core.DefaultDirectoryService;
import org.apache.directory.server.core.DirectoryService;
import org.apache.directory.server.core.partition.Partition;
import org.apache.directory.server.core.partition.impl.btree.jdbm.JdbmPartition;
import org.apache.directory.server.core.partition.ldif.LdifPartition;
import org.apache.directory.server.core.schema.SchemaPartition;
import org.apache.directory.server.ldap.LdapServer;
import org.apache.directory.server.protocol.shared.transport.TcpTransport;
import org.apache.directory.shared.ldap.entry.ServerEntry;
import org.apache.directory.shared.ldap.exception.LdapInvalidDnException;
import org.apache.directory.shared.ldap.name.DN;
import org.apache.directory.shared.ldap.schema.SchemaManager;
import org.apache.directory.shared.ldap.schema.ldif.extractor.SchemaLdifExtractor;
import org.apache.directory.shared.ldap.schema.ldif.extractor.impl.DefaultSchemaLdifExtractor;
import org.apache.directory.shared.ldap.schema.loader.ldif.LdifSchemaLoader;
import org.apache.directory.shared.ldap.schema.manager.impl.DefaultSchemaManager;
import org.apache.directory.shared.ldap.schema.registries.SchemaLoader;
import org.eclipse.che.api.core.util.CustomPortService;

import javax.naming.NamingException;
import java.io.File;
import java.io.IOException;

import static java.nio.file.Files.createTempDirectory;
import static org.eclipse.che.commons.lang.IoUtil.deleteRecursive;

/**
 * @author Yevhenii Voevodin
 */
public class MyLdapServer {

    private static final CustomPortService PORT_SERVICE = new CustomPortService(8000, 10000);

    public static Builder builder() {
        return new Builder();
    }

    private LdapServer       ldapServer;
    private DirectoryService service;
    private File             workingDir;
    private String           url;
    private int              port;
    private DN               baseDn;

    public MyLdapServer(File workingDir,
                        String partitionDn,
                        String partitionId,
                        int port,
                        boolean enableChangelog,
                        boolean allowAnonymousAccess) throws Exception {
        this.workingDir = workingDir;
        this.baseDn = new DN(partitionDn);
        this.port = port > 0 ? port : PORT_SERVICE.acquire();
        this.url = "ldap://localhost:" + this.port;
        ldapServer = new LdapServer();
        ldapServer.setTransports(new TcpTransport(this.port));
        ldapServer.setDirectoryService(service = initDirectoryService(workingDir,
                                                                      partitionId,
                                                                      partitionDn,
                                                                      enableChangelog,
                                                                      allowAnonymousAccess));
    }

    public void start() throws Exception {
        ldapServer.start();
    }

    public void stop() {
        ldapServer.stop();
        PORT_SERVICE.release(port);
        deleteRecursive(workingDir);
    }

    public String getUrl() {
        return url;
    }

    public ServerEntry createEntry(String name, String value) throws Exception {
        return service.newEntry(new DN(name + '=' + value + ',' + baseDn.toString()));
    }

    public void addEntry(ServerEntry entry) throws Exception {
        service.getSession().add(entry);
    }

    public DirectoryService getService() {
        return service;
    }

    private static DirectoryService initDirectoryService(File workingDir,
                                                         String partitionId,
                                                         String partitionDn,
                                                         boolean enableChangelog,
                                                         boolean allowAnonymousAccess) throws Exception {
        final DirectoryService service = new DefaultDirectoryService();
        service.setShutdownHookEnabled(false);
        service.setAllowAnonymousAccess(allowAnonymousAccess);
        service.getChangeLog().setEnabled(enableChangelog);
        service.setWorkingDirectory(workingDir);

        // Init schema manager
        SchemaPartition schemaPartition = service.getSchemaService().getSchemaPartition();
        LdifPartition ldifPartition = new LdifPartition();
        String workDirectory = service.getWorkingDirectory().getPath();
        ldifPartition.setWorkingDirectory(workDirectory + "/schema");
        SchemaLdifExtractor extractor = new DefaultSchemaLdifExtractor(new File(workDirectory));
        extractor.extractOrCopy(true);
        schemaPartition.setWrappedPartition(ldifPartition);
        SchemaLoader schemaLoader = new LdifSchemaLoader(new File(workDirectory + "/schema"));
        SchemaManager schemaManager = new DefaultSchemaManager(schemaLoader);
        schemaPartition.setSchemaManager(schemaManager);
        service.setSchemaManager(schemaManager);
        schemaManager.loadAllEnabled();

        // Add system partition
        final Partition systemPartition = addPartition(service, "system", ServerDNConstants.SYSTEM_DN);
        service.setSystemPartition(systemPartition);

        // Add default partition from configuration
        final Partition partition = addPartition(service, partitionId, partitionDn);

        // Startup the service
        service.startup();

        // Add root entry
        final CoreSession session = service.getAdminSession();
        if (!session.exists(partition.getSuffixDn())) {
            final DN dn = new DN(partitionDn);
            final ServerEntry rootEntry = service.newEntry(dn);
            rootEntry.add("objectClass", "top", "domain", "extensibleObject");
            rootEntry.add("dc", partitionId);
            session.add(rootEntry);
        }
        return service;
    }

    private static Partition addPartition(DirectoryService service, String partitionId, String partitionDn) throws Exception {
        JdbmPartition partition = new JdbmPartition();
        partition.setId(partitionId);
        partition.setPartitionDir(new File(service.getWorkingDirectory(), partitionId));
        partition.setSuffix(partitionDn);
        service.addPartition(partition);
        return partition;
    }

    public static class Builder {

        private int     port;
        private boolean allowAnonymousAccess;
        private boolean enableChangelog;
        private File    workingDir;
        private String  partitionId;
        private String  partitionSuffix;

        public Builder setPort(int port) {
            this.port = port;
            return this;
        }


        public Builder setWorkingDir(File workingDir) {
            this.workingDir = workingDir;
            return this;
        }

        public Builder useTmpWorkingDir() throws IOException {
            return setWorkingDir(createTempDirectory("ldap-server").toFile());
        }

        public Builder allowAnonymousAccess() {
            this.allowAnonymousAccess = true;
            return this;
        }

        public Builder enableChangelog() {
            this.enableChangelog = true;
            return this;
        }

        public Builder setPartitionId(String partitionId) {
            this.partitionId = partitionId;
            return this;
        }

        public Builder setPartitionDn(String partitionSuffix) {
            this.partitionSuffix = partitionSuffix;
            return this;
        }

        public MyLdapServer build() throws Exception {
            return new MyLdapServer(workingDir,
                                    partitionSuffix,
                                    partitionId,
                                    port,
                                    enableChangelog,
                                    allowAnonymousAccess);
        }
    }
}
