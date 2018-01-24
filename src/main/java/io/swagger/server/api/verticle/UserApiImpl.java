package io.swagger.server.api.verticle;

import io.swagger.server.api.model.User;
import io.swagger.server.api.service.UserApi;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.ext.mongo.MongoClient;

public class UserApiImpl implements UserApi {
    private static final String COLLECTION = "users";

    @Override
    public MongoClient userPost(MongoClient mongoClient, User user, Handler<AsyncResult<String>> handler) {
        user = user.generateUserId();
        user.setUserId(user.getUserId());
        return mongoClient.insert(COLLECTION, user.toJson(), handler);
    }
}
