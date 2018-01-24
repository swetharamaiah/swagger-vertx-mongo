package io.swagger.server.api.verticle;

import io.swagger.server.api.model.Project;
import io.swagger.server.api.service.ProjectsApi;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.mongo.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class ProjectsApiImpl implements ProjectsApi {

    private static final String COLLECTION = "projects";

    final static Logger LOGGER = LoggerFactory.getLogger(ProjectsApiImpl.class);

    @Override
    public MongoClient addProject(MongoClient mongoClient, Project newProject, Handler<AsyncResult<String>> handler) {
        UUID id = UUID.randomUUID();
        newProject.setId(id.toString());
        newProject.setStatus(Project.StatusEnum.OPEN);
        return mongoClient.insert(COLLECTION, newProject.toJson(), handler);
    }

    @Override
    public MongoClient getProjectById(MongoClient mongoClient, String projectId, Handler<AsyncResult<JsonObject>> handler) {
        return mongoClient.findOne(COLLECTION, new JsonObject().put("_id", projectId), null, handler);
    }

    @Override
    public MongoClient getProjects(MongoClient mongoClient, String status, Handler<AsyncResult<List<JsonObject>>> handler) {
        JsonObject query = new JsonObject();
        if(status != null) {
            query.put("status", status);
        }

        return mongoClient.find(COLLECTION, query, handler);
    }

    @Override
    public MongoClient closeExpiringProjects(MongoClient mongoClient, Project.StatusEnum statusEnum, Handler<AsyncResult<MongoClientUpdateResult>> handler) {
        JsonObject query = new JsonObject().put("biddingExpiration",
                new JsonObject().put("$lte", Instant.now().toString()));
        JsonObject update = new JsonObject().put("$set", new JsonObject().put("status", statusEnum));
        UpdateOptions updateOptions = new UpdateOptions();
        updateOptions.setMulti(true);
        return mongoClient.updateCollectionWithOptions(COLLECTION, query, update, updateOptions, handler);
    }


    @Override
    public MongoClient updateProject(MongoClient mongoClient, JsonObject query, JsonObject update, Handler<AsyncResult<MongoClientUpdateResult>> handler) {
        return mongoClient.updateCollection(COLLECTION, query, update, handler);
    }

    @Override
    public void updateToLowestBidAmount(MongoClient mongoClient, Project project) {
        JsonObject subCommand = new JsonObject().
                put("$match", new JsonObject().put("_id", project.getId()))
                .put("$project", new JsonObject()
                        .put("minBid", new JsonObject()
                                .put("$min", "$bids.bidAmount")));

        JsonObject command = new JsonObject().put("aggregate", COLLECTION)
                .put("cursor", new JsonObject())
                .put("pipeline", new JsonArray().add(subCommand));
        mongoClient.runCommand("aggregate", command, result -> {
            if (result.succeeded()) {
                if (result.result() != null && !result.result().isEmpty()) {
                    JsonArray values = result.result().getJsonObject("cursor")
                            .getJsonArray("firstBatch");
                    for (Object v : values) {
                        String projectId = ((JsonObject) v).getString("_id");
                        String minBidAmount = ((JsonObject) v).getString("minBid");
                        if (projectId != null && minBidAmount != null) {
                            Project p = new Project(projectId, Double.valueOf(minBidAmount));
                            JsonObject query = new JsonObject().put("_id", project.getId());
                            JsonObject update = new JsonObject().put("$set", new JsonObject().put("lowestBidAmount", p.getLowestBidAmount()));
                            updateProject(mongoClient, query, update, updateResultAsyncResult -> {
                                if (updateResultAsyncResult.succeeded()) {
                                    LOGGER.info("Updated Lowest Bid Amount for projectId: " + projectId);
                                } else {
                                    LOGGER.error("Error occurred while updating lowest Bid amount on projectId: " + projectId, result.cause());
                                }
                            });
                        }

                    }
                }
            }
        });
    }

}