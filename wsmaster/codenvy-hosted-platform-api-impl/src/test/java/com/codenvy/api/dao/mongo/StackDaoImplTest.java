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
package com.codenvy.api.dao.mongo;

import com.codenvy.api.dao.mongo.stack.StackDaoImpl;
import com.codenvy.api.dao.mongo.stack.StackImplCodec;
import com.github.fakemongo.Fongo;
import com.google.common.collect.ImmutableSet;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import org.bson.Document;
import org.bson.codecs.DocumentCodec;
import org.bson.codecs.configuration.CodecRegistry;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.acl.AclEntryImpl;
import org.eclipse.che.api.core.model.machine.Command;
import org.eclipse.che.api.core.model.project.ProjectConfig;
import org.eclipse.che.api.machine.server.model.impl.CommandImpl;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentRecipeImpl;
import org.eclipse.che.api.workspace.server.model.impl.ExtendedMachineImpl;
import org.eclipse.che.api.workspace.server.model.impl.ProjectConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.ServerConf2Impl;
import org.eclipse.che.api.workspace.server.model.impl.SourceStorageImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.stack.StackComponentImpl;
import org.eclipse.che.api.workspace.server.model.impl.stack.StackImpl;
import org.eclipse.che.api.workspace.server.model.impl.stack.StackSourceImpl;
import org.eclipse.che.api.workspace.shared.stack.StackComponent;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static com.codenvy.api.dao.mongo.MongoUtil.documentsListAsMap;
import static com.codenvy.api.dao.mongo.MongoUtilTest.mockWriteEx;
import static com.mongodb.ErrorCategory.DUPLICATE_KEY;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.bson.codecs.configuration.CodecRegistries.fromCodecs;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

/**
 * Test for {@link StackDaoImpl}
 *
 * @author Alexander Andrienko
 */
@Listeners(value = {MockitoTestNGListener.class})
public class StackDaoImplTest extends BaseDaoTest {
    private static final String BD_COLLECTION_NAME = "stacks";
    private MongoCollection<StackImpl> collection;

    //variables for init test stackTest
    private static final String ID_TEST           = "randomId123";
    private static final String NAME              = "Java";
    private static final String DESCRIPTION       = "Simple java stackTest for generation java projects";
    private static final String USER_ID           = "user123";
    private static final String CREATOR           = USER_ID;
    private static final String SCOPE             = "advanced";
    private static final String SOURCE_TYPE       = "image";
    private static final String SOURCE_ORIGIN     = "codenvy/ubuntu_jdk8";
    private static final String COMPONENT_NAME    = "Java";
    private static final String COMPONENT_VERSION = "1.8_45";

    private List<String>        tags      = asList("java", "maven");
    private WorkspaceConfigImpl workspace = createWorkspace();
    private StackImpl stackTest;
    private StackImpl stackTest2;

    private StackDaoImpl stackDao;

    @Captor
    ArgumentCaptor<Document> documentHolderCaptor;

    @BeforeMethod
    public void setUp() throws ServerException {
        final Fongo fongo = new Fongo("Stack test server");

        CodecRegistry codecRegistry = MongoClient.getDefaultCodecRegistry();
        codecRegistry = fromRegistries(codecRegistry, fromCodecs(new AclEntryImplCodec(codecRegistry)));
        codecRegistry = fromRegistries(codecRegistry, fromCodecs(new StackImplCodec(codecRegistry)));

        database = fongo.getDatabase("stacks").withCodecRegistry(codecRegistry);
        collection = database.getCollection(BD_COLLECTION_NAME, StackImpl.class);

        stackDao = new StackDaoImpl(database, "stacks");

        List<String> tags = asList("Java", "Maven");
        StackComponentImpl stackComponent = new StackComponentImpl("some component", "1.0.0");
        StackSourceImpl stackSource = new StackSourceImpl("location", "http://some/url");
        stackTest = StackImpl.builder().setId("testId")
                             .setName("name")
                             .setCreator("creator")
                             .setDescription("description")
                             .setScope("advanced")
                             .setTags(tags)
                             .setComponents(singletonList(stackComponent))
                             .setSource(stackSource)
                             .setAcl(singletonList(new AclEntryImpl("creator", asList("search", "delete"))))
                             .build();
        stackTest2 = StackImpl.builder()
                              .setId("testId2")
                              .setCreator("creator")
                              .setScope("advanced")
                              .setSource(stackSource)
                              .setTags(tags)
                              .setAcl(singletonList(new AclEntryImpl("creator", asList("search", "delete"))))
                              .build();
    }

    @Test
    public void stackShouldBeCreated() throws ConflictException, ServerException {
        stackDao.create(stackTest);

        StackImpl StackImpl = collection.find(Filters.eq("_id", stackTest.getId())).first();

        assertEquals(StackImpl, stackTest);
    }

    @Test(expectedExceptions = ConflictException.class, expectedExceptionsMessageRegExp = "Stack with id 'testId' already exists")
    public void shouldThrowConflictExceptionWhenStackIsAlreadyCreated() throws ConflictException, ServerException {
        final MongoDatabase db = mockDatabase(col -> doThrow(mockWriteEx(DUPLICATE_KEY)).when(col).insertOne(any()));
        new StackDaoImpl(db, "stacks").create(stackTest);
    }

    @Test(expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = "Stack required")
    public void shouldThrowNullPointerExceptionWhenStackForCreationIsNull() throws ConflictException, ServerException {
        stackDao.create(null);
    }

    @Test
    public void stackByIdShouldBeReturned() throws NotFoundException, ServerException {
        collection.insertOne(stackTest);

        StackImpl result = stackDao.getById("testId");

        assertEquals(stackTest, result);
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void shouldThrowNotFoundExceptionWhenStackByIdWasNotFound() throws NotFoundException, ServerException {
        stackDao.getById("id");
    }

    @Test(expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = "Stack id required")
    public void shouldThrowNullPointerExceptionWhenStackIsNull() throws NotFoundException, ServerException {
        stackDao.getById(null);
    }

    @Test(expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = "Stack id required")
    public void shouldThrowNullPointerExceptionWhenWeTryRemoveNull() throws ServerException {
        stackDao.remove(null);
    }

    @Test
    public void stackShouldBeRemoved() throws ServerException {
        collection.insertOne(stackTest);

        stackDao.remove(stackTest.getId());

        StackImpl stackImpl = collection.find(Filters.eq("_id", stackTest.getId())).first();

        assertNull(stackImpl);
    }

    @Test
    public void stackShouldBeUpdated() throws NotFoundException, ServerException {
        collection.insertOne(stackTest);

        StackImpl updateStack = new StackImpl(stackTest);
        updateStack.setName("NewName");
        updateStack.setScope("general");

        stackDao.update(updateStack);

        StackImpl expected = collection.find(Filters.eq("_id", stackTest.getId())).first();
        assertEquals(updateStack, expected);
    }

    @Test(expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = "Stack for updating required")
    public void shouldThrowNullPointerExceptionWhenWeTryUpdateNullStack() throws NotFoundException, ServerException {
        stackDao.update(null);
    }

    @Test(expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = "Required non-null stack id")
    public void shouldThrowNullPointerExceptionWhenIdStackForUpdateIsNull() throws NotFoundException, ServerException {
        stackDao.update(StackImpl.builder().build());
    }

    @Test(expectedExceptions = NotFoundException.class, expectedExceptionsMessageRegExp = "Stack with id 'testId' was not found")
    public void shouldThrowNotFoundExceptionWhenStackTargetForUpdateIsNull() throws NotFoundException, ServerException {
        stackDao.update(stackTest);
    }

    @Test
    public void stacksByTagsShouldBeFound() throws ServerException {
        collection.insertOne(stackTest);
        collection.insertOne(stackTest2);

        List<String> tags = singletonList("Java");
        List<StackImpl> stackImpls = stackDao.searchStacks("creator", tags, 0, 30);

        assertEquals(stackImpls.size(), 2);
        assertEquals(new HashSet<>(stackImpls), ImmutableSet.of(stackTest, stackTest2));
    }

    @Test
    public void shouldReturnOneStackByTags() throws ServerException {
        collection.insertOne(stackTest);
        collection.insertOne(stackTest2);

        List<String> tags = singletonList("Java");
        List<StackImpl> stackImpls = stackDao.searchStacks("creator", tags, 0, 1);

        assertEquals(stackImpls.size(), 1);
        assertEquals(stackImpls.get(0), stackTest);
    }

    // suppress warnings about unchecked cast because we have to cast Mongo documents to lists
    @SuppressWarnings("unchecked")
    @Test
    public void encodeStackTest() {
        // mocking DocumentCodec
        final DocumentCodec documentCodec = mock(DocumentCodec.class);
        when(documentCodec.getEncoderClass()).thenReturn(Document.class);

        CodecRegistry codecRegistry = fromCodecs(documentCodec);
        codecRegistry = fromRegistries(codecRegistry, fromCodecs(new AclEntryImplCodec(codecRegistry)));
        StackImplCodec stackImplCodec = new StackImplCodec(codecRegistry);

        //prepare test stackTest
        final StackImpl stack = createStack();

        //launch test action
        stackImplCodec.encode(null, stack, null);

        verify(documentCodec).encode(any(), documentHolderCaptor.capture(), any());
        Document stackDocument = documentHolderCaptor.getValue();

        // check encoding result
        assertEquals(stackDocument.getString("_id"), stack.getId(), "Stack id");
        assertEquals(stackDocument.getString("name"), stack.getName(), "Stack name");
        assertEquals(stackDocument.getString("description"), stack.getDescription(), "Stack description");
        assertEquals(stackDocument.getString("scope"), stack.getScope(), "Stack scope");
        assertEquals(stackDocument.getString("creator"), stack.getCreator(), "Stack creator");

        assertEquals((List<String>)stackDocument.get("tags"), tags, "Stack tags");

        Document sourceDocument = (Document)stackDocument.get("source");
        assertEquals(sourceDocument.getString("type"), SOURCE_TYPE, "Stack source type");
        assertEquals(sourceDocument.getString("origin"), SOURCE_ORIGIN, "Stack source origin");

        List<Document> components = (List<Document>)stackDocument.get("components");
        Document componentDocument = components.get(0);
        assertEquals(componentDocument.getString("name"), COMPONENT_NAME, "Stack component type");
        assertEquals(componentDocument.getString("version"), COMPONENT_VERSION, "Stack component origin");

        //verify workspaceConfig
        Document workspaceDocument = (Document)stackDocument.get("workspaceConfig");

        assertEquals(workspaceDocument.getString("name"), workspace.getName(), "Workspace name");
        assertEquals(workspaceDocument.getString("description"), workspace.getDescription(), "Workspace description");
        assertEquals(workspaceDocument.getString("defaultEnv"), workspace.getDefaultEnv(), "Workspace defaultEnvName");

        // check commands
        final List<Document> commands = (List<Document>)workspaceDocument.get("commands");
        assertEquals(commands.size(), workspace.getCommands().size(), "Workspace commands size");
        for (int i = 0; i < commands.size(); i++) {
            final Command command = workspace.getCommands().get(i);
            final Document document = commands.get(i);

            assertEquals(document.getString("name"), command.getName(), "Command name");
            assertEquals(document.getString("commandLine"), command.getCommandLine(), "Command line");
            assertEquals(document.getString("type"), command.getType(), "Command type");
        }

        // check projects
        final List<Document> projects = (List<Document>)workspaceDocument.get("projects");
        assertEquals(projects.size(), workspace.getProjects().size());
        for (int i = 0; i < projects.size(); i++) {
            final ProjectConfig project = workspace.getProjects().get(i);
            final Document projDoc = projects.get(0);

            assertEquals(project.getName(), projDoc.getString("name"), "Project nam");
            assertEquals(project.getType(), projDoc.getString("type"), "Project type");
            assertEquals(project.getDescription(), projDoc.getString("description"));
            assertEquals(project.getPath(), projDoc.getString("path"));

            final List<Document> mixins = (List<Document>)projDoc.get("mixins");
            assertEquals(project.getMixins(), mixins, "Mixin types");

            final List<Document> attrsList = (List<Document>)projDoc.get("attributes");
            assertEquals(attrsList.size(), project.getAttributes().size());
            for (Document attrDoc : attrsList) {
                final String attrName = attrDoc.getString("name");
                final List<String> attrValue = (List<String>)attrDoc.get("value");

                assertEquals(project.getAttributes().get(attrName), attrValue, "Attribute values");
            }

            if (project.getSource() != null) {
                final Document source = (Document)projDoc.get("source");

                assertNotNull(source);
                assertEquals(source.getString("type"), project.getSource().getType(), "Source type");
                assertEquals(source.getString("location"), project.getSource().getLocation(), "Source location");

                final List<Document> parameters = (List<Document>)source.get("parameters");
                assertEquals(documentsListAsMap(parameters), project.getSource().getParameters(), "Source parameters");
            }
        }

        // check environments
        final Map<String, Document> environments = (Map<String, Document>)workspaceDocument.get("environments");
        assertEquals(environments.size(), workspace.getEnvironments().size());
        for (Map.Entry<String, Document> envEntry : environments.entrySet()) {
            final EnvironmentImpl environment = workspace.getEnvironments()
                                                         .get(envEntry.getKey());
            assertNotNull(environment);
            if (environment.getRecipe() != null) {
                final Document document = envEntry.getValue().get("recipe", Document.class);
                assertEquals(document.getString("type"),
                             environment.getRecipe().getType(),
                             "Environment recipe type");
                assertEquals(document.getString("contentType"),
                             environment.getRecipe().getContentType(),
                             "Environment recipe content type");
                assertEquals(document.getString("content"),
                             environment.getRecipe().getContent(),
                             "Environment recipe content");
                assertEquals(document.getString("location"),
                             environment.getRecipe().getLocation(),
                             "Environment recipe location");
            }
            if (environment.getMachines() != null) {
                Map<String, Document> machinesDocs = (Map<String, Document>)envEntry.getValue().get("machines");
                for (Map.Entry<String, ExtendedMachineImpl> machineEntry : environment.getMachines()
                                                                                      .entrySet()) {

                    if (machineEntry.getValue() != null) {
                        Document machineDoc = machinesDocs.get(machineEntry.getKey());
                        assertNotNull(machineDoc);
                        ExtendedMachineImpl machine = machineEntry.getValue();
                        if (machine.getAgents() != null) {
                            List<String> agents = (List<String>)machineDoc.get("agents");
                            assertEquals(agents, machine.getAgents());
                        }
                        if (machine.getServers() != null) {
                            Map<String, Document> serversDocs = (Map<String, Document>)machineDoc.get("servers");
                            for (Map.Entry<String, ServerConf2Impl> serverEntry : machine.getServers()
                                                                                         .entrySet()) {
                                if (serverEntry.getValue() != null) {
                                    Document serverDoc = serversDocs.get(serverEntry.getKey());
                                    assertNotNull(serverDoc);

                                    assertEquals(serverDoc.getString("port"), serverEntry.getValue().getPort());

                                    assertEquals(serverDoc.getString("protocol"), serverEntry.getValue().getProtocol());

                                    List<Document> properties = (List<Document>)serverDoc.get("properties");
                                    if (serverEntry.getValue().getProperties() != null) {
                                        assertEquals(documentsListAsMap(properties),
                                                     serverEntry.getValue().getProperties(),
                                                     "Server properties");
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Test
    public void decodeStackTest() {
        // mocking DocumentCodec
        final DocumentCodec documentCodec = mock(DocumentCodec.class);
        when(documentCodec.getEncoderClass()).thenReturn(Document.class);
        CodecRegistry codecRegistry = fromCodecs(documentCodec);
        codecRegistry = fromRegistries(codecRegistry, fromCodecs(new AclEntryImplCodec(codecRegistry)));
        StackImplCodec stackImplCodec = new StackImplCodec(codecRegistry);

        // prepare test workspace
        final StackImpl stack = createStack();

        // encode workspace
        stackImplCodec.encode(null, stack, null);

        verify(documentCodec).encode(any(), documentHolderCaptor.capture(), any());
        Document document = documentHolderCaptor.getValue();

        // mocking document codec to return encoded workspace
        when(documentCodec.decode(any(), any())).thenReturn(document);

        final StackImpl result = stackImplCodec.decode(null, null);

        assertEquals(result, stack);
    }

    private StackImpl createStack() {
        List<StackComponent> componentsImpl = singletonList(new StackComponentImpl(COMPONENT_NAME, COMPONENT_VERSION));
        StackSourceImpl source = new StackSourceImpl(SOURCE_TYPE, SOURCE_ORIGIN);
        return StackImpl.builder()
                        .setId(ID_TEST)
                        .setName(NAME)
                        .setDescription(DESCRIPTION)
                        .setScope(SCOPE)
                        .setCreator(CREATOR)
                        .setTags(tags)
                        .setSource(source)
                        .setWorkspaceConfig(workspace)
                        .setComponents(componentsImpl)
                        .setAcl(singletonList(new AclEntryImpl(CREATOR, asList("read", "delete"))))
                        .build();
    }

    private static WorkspaceConfigImpl createWorkspace() {
        // environments
        Map<String, EnvironmentImpl> environments = new HashMap<>();

        Map<String, ExtendedMachineImpl> machines;
        Map<String, ServerConf2Impl> servers;
        Map<String, String> properties;
        EnvironmentImpl env;

        servers = new HashMap<>();
        properties = new HashMap<>();
        properties.put("prop1", "value1");
        properties.put("prop2", "value2");
        servers.put("ref1", new ServerConf2Impl("port1", "proto1", properties));
        properties = new HashMap<>();
        properties.put("prop3", "value3");
        properties.put("prop4", "value4");
        servers.put("ref2", new ServerConf2Impl("port2", "proto2", properties));
        machines = new HashMap<>();
        machines.put("machine1", new ExtendedMachineImpl(asList("org.eclipse.che.ws-agent", "someAgent"),
                                                         servers,
                                                         new HashMap<>(singletonMap("memoryLimitBytes", "10000"))));
        servers = new HashMap<>();
        properties = new HashMap<>();
        properties.put("prop5", "value5");
        properties.put("prop6", "value6");
        servers.put("ref3", new ServerConf2Impl("port3", "proto3", properties));
        properties = new HashMap<>();
        properties.put("prop7", "value7");
        properties.put("prop8", "value8");
        servers.put("ref4", new ServerConf2Impl("port4", "proto4", properties));
        machines = new HashMap<>();
        machines.put("machine2", new ExtendedMachineImpl(asList("ws-agent2", "someAgent2"),
                                                         servers,
                                                         new HashMap<>(singletonMap("memoryLimitBytes", "10000"))));
        env = new EnvironmentImpl();
        env.setRecipe(new EnvironmentRecipeImpl("type", "contentType", "content", null));
        env.setMachines(machines);

        environments.put("my-environment", env);

        env = new EnvironmentImpl();
        servers = new HashMap<>();
        properties = new HashMap<>();
        servers.put("ref11", new ServerConf2Impl("port11", "proto11", properties));
        servers.put("ref12", new ServerConf2Impl("port12", "proto12", null));
        machines = new HashMap<>();
        machines.put("machine11", new ExtendedMachineImpl(emptyList(),
                                                          servers,
                                                          new HashMap<>(singletonMap("memoryLimitBytes", "10000"))));
        servers.put("ref13", new ServerConf2Impl("port13", "proto13", singletonMap("prop11", "value11")));
        servers.put("ref14", new ServerConf2Impl("port4", null, null));
        servers.put("ref15", new ServerConf2Impl(null, null, null));
        machines.put("machine12", new ExtendedMachineImpl(null,
                                                          servers,
                                                          new HashMap<>(singletonMap("memoryLimitBytes", "10000"))));
        machines.put("machine13", new ExtendedMachineImpl(null,
                                                          null,
                                                          new HashMap<>(singletonMap("memoryLimitBytes", "10000"))));
        env.setRecipe(new EnvironmentRecipeImpl("type", "contentType", "content", null));
        env.setMachines(machines);

        environments.put("my-environment-2", env);

        env = new EnvironmentImpl();
        env.setRecipe(new EnvironmentRecipeImpl(null, null, null, null));
        env.setMachines(null);

        environments.put("my-environment-3", env);

        // projects
        final ProjectConfigImpl project1 = new ProjectConfigImpl();
        project1.setName("test-project-name");
        project1.setDescription("This is test project");
        project1.setPath("/path/to/project");
        project1.setType("maven");
        project1.setMixins(singletonList("git"));

        final Map<String, List<String>> projectAttrs = new HashMap<>(4);
        projectAttrs.put("project.attribute1", singletonList("value1"));
        projectAttrs.put("project.attribute2", asList("value2", "value3"));
        project1.setAttributes(projectAttrs);

        final Map<String, String> sourceParameters = new HashMap<>(4);
        sourceParameters.put("source-parameter-1", "value1");
        sourceParameters.put("source-parameter-2", "value2");
        project1.setSource(new SourceStorageImpl("sources-type", "sources-location", sourceParameters));

        final List<ProjectConfigImpl> projects = singletonList(project1);

        // commands
        final List<CommandImpl> commands = new ArrayList<>(3);
        commands.add(new CommandImpl("MCI", "mvn clean install", "maven"));
        commands.add(new CommandImpl("bower install", "bower install", "bower"));
        commands.add(new CommandImpl("build without tests", "mvn clean install -Dmaven.test.skip", "maven"));

        return WorkspaceConfigImpl.builder()
                                  .setName("workspace-name")
                                  .setDescription("This is test workspace")
                                  .setCommands(commands)
                                  .setProjects(projects)
                                  .setEnvironments(environments)
                                  .setDefaultEnv("my-environment")
                                  .build();
    }

    private MongoDatabase mockDatabase(Consumer<MongoCollection<StackImpl>> consumer) {
        @SuppressWarnings("unchecked")
        final MongoCollection<StackImpl> collection = mock(MongoCollection.class);
        consumer.accept(collection);

        final MongoDatabase database = mock(MongoDatabase.class);
        when(database.getCollection("stacks", StackImpl.class)).thenReturn(collection);

        return database;
    }
}
