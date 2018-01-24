package io.swagger.server.api.service;

import io.swagger.server.api.model.User;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.ext.mongo.MongoClient;

public interface UserApi  {
    //POST_user
    MongoClient userPost(MongoClient mongoClient, User user, Handler<AsyncResult<String>> handler);

}
