package io.swagger.server.api.service;

import io.swagger.server.api.MainApiException;

import io.swagger.server.api.model.Bid;
import io.swagger.server.api.model.Project;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.mongo.MongoClientUpdateResult;

import java.util.List;
import java.util.Map;

public interface BidApi  {
    //POST_bids
    MongoClient bidsPost(MongoClient mongoClient, Project project, Bid newBid, Handler<AsyncResult<MongoClientUpdateResult>> handler);
}
