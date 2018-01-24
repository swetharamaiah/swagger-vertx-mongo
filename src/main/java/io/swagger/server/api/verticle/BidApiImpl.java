package io.swagger.server.api.verticle;

import io.swagger.server.api.model.Bid;
import io.swagger.server.api.model.Project;
import io.swagger.server.api.service.BidApi;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.mongo.MongoClientUpdateResult;

public class BidApiImpl implements BidApi {

    private static final String COLLECTION = "projects";

    @Override
    public MongoClient bidsPost(MongoClient mongoClient, Project project, Bid newBid, Handler<AsyncResult<MongoClientUpdateResult>> handler) {
        String projectId = newBid.getProjectId();
        JsonObject query = new JsonObject().put("_id",projectId);
        JsonObject update = new JsonObject().put("$addToSet",
                new JsonObject().put("bids",newBid.toJson()));


        return mongoClient.updateCollection(COLLECTION, query, update, handler);
    }
}
