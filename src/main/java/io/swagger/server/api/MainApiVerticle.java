package io.swagger.server.api;

import java.nio.charset.Charset;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.phiz71.vertx.swagger.router.OperationIdServiceIdResolver;
import com.github.phiz71.vertx.swagger.router.SwaggerRouter;

import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;
import io.vertx.core.*;
import io.vertx.core.file.FileSystem;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;

public class MainApiVerticle extends AbstractVerticle {
    final static Logger LOGGER = LoggerFactory.getLogger(MainApiVerticle.class); 
    
    protected Router router;

    @Override
    public void init(Vertx vertx, Context context) {
        super.init(vertx, context);
        router = Router.router(vertx);
    }

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        Json.mapper.registerModule(new JavaTimeModule());
        FileSystem vertxFileSystem = vertx.fileSystem();
        vertxFileSystem.readFile("swagger.json", readFile -> {
            if (readFile.succeeded()) {
                Swagger swagger = new SwaggerParser().parse(readFile.result().toString(Charset.forName("utf-8")));
                Router swaggerRouter = SwaggerRouter.swaggerRouter(router, swagger, vertx.eventBus(),
                        new OperationIdServiceIdResolver());
                deployVerticles(startFuture);
                
                vertx.createHttpServer() 
                    .requestHandler(swaggerRouter::accept) 
                    .listen(8283);
                startFuture.complete();
            } else {
            	startFuture.fail(readFile.cause());
            }
        });        		        
    }
      
    public void deployVerticles(Future<Void> startFuture) {
        int port = 8283;
        DeploymentOptions options = new DeploymentOptions().setConfig(new JsonObject()
                .put("http.port",port)
                .put("db_name", "vertx-mongo"));

        vertx.deployVerticle("io.swagger.server.api.verticle.BidApiVerticle", res -> {
            if (res.succeeded()) {
                LOGGER.info("BidApiVerticle : Deployed");
            } else {
                startFuture.fail(res.cause());
                LOGGER.error("BidApiVerticle : Deployment failed");
            }
        });

        vertx.deployVerticle("io.swagger.server.api.verticle.ProjectsApiVerticle", options, res -> {
            if (res.succeeded()) {
                LOGGER.info("ProjectsApiVerticle : Deployed");
            } else {
                startFuture.fail(res.cause());
                LOGGER.error("ProjectsApiVerticle : Deployment failed");
            }
        });
        
        vertx.deployVerticle("io.swagger.server.api.verticle.UserApiVerticle", res -> {
            if (res.succeeded()) {
                LOGGER.info("UserApiVerticle : Deployed");
            } else {
                startFuture.fail(res.cause());
                LOGGER.error("UserApiVerticle : Deployment failed");
            }
        });

    }

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new MainApiVerticle());
    }
}