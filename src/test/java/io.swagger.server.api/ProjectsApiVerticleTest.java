package io.swagger.server.api;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import io.swagger.server.api.model.Project;
import io.swagger.server.api.service.ProjectsApi;
import io.swagger.server.api.verticle.ProjectsApiImpl;
import io.swagger.server.api.verticle.ProjectsApiVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Date;
import java.util.UUID;

@RunWith(VertxUnitRunner.class)
@PrepareForTest(MongoClient.class)
public class ProjectsApiVerticleTest {

    private ProjectsApi projectsApi;
    private static Vertx vertx;
    private static EventBus eventBus;
    private static JsonArray definitions;

    private int port;
    private static MongodProcess MONGO;
    private static int MONGO_PORT = 8283;
    String uuid;
    MongoClient mongoClient;
    @Before
    public void setUp(TestContext context) {
        vertx = Vertx.vertx();
        ServerSocket socket = null;

        projectsApi = new ProjectsApiImpl();
        uuid = UUID.randomUUID().toString();
        try {
            socket = new ServerSocket(0);
            port = socket.getLocalPort();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
            vertx.close(context.asyncAssertSuccess());
        }
        DeploymentOptions options = new DeploymentOptions()
                .setConfig(new JsonObject()
                                .put("http.port", port)
                                .put("db_name", "projects")
                                .put("connection_string",
                                        "mongodb://localhost:" + MONGO_PORT)
                );
        JsonObject config = new JsonObject()
                .put("http.port", port)
                .put("db_name", "projects")
                .put("connection_string",
                        "mongodb://localhost:" + MONGO_PORT);
        mongoClient = MongoClient.createShared(vertx, config);
        vertx.deployVerticle(ProjectsApiVerticle.class.getName(), options, context.asyncAssertSuccess());
    }

    @BeforeClass
    public static void initialize(TestContext context) throws IOException {
        MongodStarter starter = MongodStarter.getDefaultInstance();
        IMongodConfig mongodConfig = new MongodConfigBuilder()
                .version(Version.Main.PRODUCTION)
                .net(new Net(MONGO_PORT, Network.localhostIsIPv6()))
                .build();
        MongodExecutable mongodExecutable =
                starter.prepare(mongodConfig);

        MONGO = mongodExecutable.start();
    }

    public void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void async_behavior(TestContext context) {
        Vertx vertx = Vertx.vertx();
        context.assertEquals("foo", "foo");
        Async a1 = context.async();
        Async a2 = context.async(3);
        vertx.setTimer(100, n -> a1.complete());
        vertx.setPeriodic(100, n -> a2.countDown());
    }

    @Test
    public void testCRUD(TestContext context) {
        Async async = context.async();
        Project project = new Project("486ee1fa-fe32-4b5f-845b-73fcc32eb595", "MongoHadoop 101", "MongoHadoop Inverted Index",
                Project.StatusEnum.OPEN, new Date(), 2000.00);
        projectsApi.addProject(mongoClient, project, context.asyncAssertSuccess(v1 -> {
            System.out.println("V1: "+v1);
            projectsApi.getProjectById(mongoClient, "486ee1fa-fe32-4b5f-845b-73fcc32eb595", json1 -> {
                context.assertTrue(json1.succeeded());
                async.complete();
            });
        }));
        async.awaitSuccess(5000);
    }

    @Test
    public void testAddProject(TestContext context) {
        Async async = context.async(3);

        final String json = Json.encodePrettily(new Project(uuid, "MongoHadoop", "Inverted Index",
                Project.StatusEnum.OPEN, new Date(1516648500000L), 999.00));
        final String length = Integer.toString(json.length());

        vertx.createHttpClient().post(port, "localhost", "/jobdiscovery/v1/projects")
                .putHeader("content-type","application/json")
                .putHeader("content-length",length)
                .handler(response -> {
                    context.assertEquals(response.statusCode(), 201);
                    context.assertTrue(response.headers().get("content-type").contains("application/json"));
                    response.bodyHandler(body -> {
                        final Project project = Json.decodeValue(body.toString(), Project.class);
                        context.assertEquals(project.getName(), "MongoHadoop");
                        context.assertEquals(project.getDescription(),"Inverted Index");
                        context.assertNotNull(project.getId());
                        async.complete();
                    });
                }).write(json)
                .end();
        async.complete();
    }

    @Test
    public void testUpdateProject(TestContext context) {
        Async async = context.async();
        final String json = Json.encodePrettily(new Project(uuid, "MongoHadoop", "Inverted Index",
                Project.StatusEnum.CLOSED, new Date(1516648500000L), 800.00));
        vertx.createHttpClient().put(port, "localhost", "/jobdiscovery/v1/projects")
                .putHeader("content-type","application/json")
                .putHeader("content-length", String.valueOf(json.length()))
                .handler(response -> {
                    context.assertEquals(response.statusCode(), 201);
                    context.assertTrue(response.headers().get("content-type").contains("application/json"));
                    System.out.println("WTF");
                    response.bodyHandler(body -> {
                        final Project project = Json.decodeValue(body.toString(), Project.class);
                        context.assertEquals(project.getName(), "MongoHadoop");
                        context.assertEquals(project.getDescription(), "Inverted Index");
                        context.assertNotNull(project.getId());
                        async.complete();
                    });
                }).write(json)
                .end();
        async.complete();
    }

    @Test
    public void testUpdateToLowestBidAmt(TestContext context) {
        Async async = context.async();
        final String json = Json.encodePrettily(new Project(uuid, "MongoHadoop", "Inverted Index",
                Project.StatusEnum.CLOSED, new Date(1516648500000L), 800.00));
        vertx.createHttpClient().put(port, "localhost","/jobdiscovery/v1/projects")
                .putHeader("content-type","application/json")
                .putHeader("content-length", String.valueOf(json.length()))
                .handler(response -> {
                    context.assertEquals(response.statusCode(), 201);
                    context.assertTrue(response.headers().get("content-type").contains("application/json"));
                    System.out.println("WTF");
                    response.bodyHandler(body -> {
                        final Project project = Json.decodeValue(body.toString(), Project.class);
                        context.assertEquals(project.getName(), "MongoHadoop");
                        context.assertEquals(project.getDescription(), "Inverted Index");
                        context.assertNotNull(project.getId());
                        async.complete();
                    });
                }).write(json)
                .end();
        async.complete();
    }
}
