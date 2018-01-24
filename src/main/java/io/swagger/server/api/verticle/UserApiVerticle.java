package io.swagger.server.api.verticle;

import io.swagger.server.api.MainApiException;
import io.swagger.server.api.model.User;
import io.swagger.server.api.service.UserApi;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.mongo.MongoClient;

public class UserApiVerticle extends AbstractVerticle {
    final static Logger LOGGER = LoggerFactory.getLogger(UserApiVerticle.class); 
    
    final static String POST_USER_SERVICE_ID = "addUser";
    
    final UserApi service;

    MongoClient mongo;

    public UserApiVerticle() {
        try {
            Class serviceImplClass = getClass().getClassLoader().loadClass("io.swagger.server.api.verticle.UserApiImpl");
            service = (UserApi)serviceImplClass.newInstance();
        } catch (Exception e) {
            logUnexpectedError("UserApiVerticle constructor", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void start() throws Exception {

        mongo = MongoClient.createShared(vertx, config());

        //Consumer for POST_user
        vertx.eventBus().<JsonObject> consumer(POST_USER_SERVICE_ID).handler(message -> {
            try {
                User user  = Json.mapper.readValue(message.body().getJsonObject("newUser").encode(), User.class);
                service.userPost(mongo, user, result -> {
                    if (result.succeeded()){
                        message.reply(Json.encodePrettily(user.setUserId(result.result())),
                                new DeliveryOptions().addHeader("content-type", "application/json; charset=urf-8"));
                    }
                    else {
                        message.fail(UserApiException.INTERNAL_SERVER_ERROR.getStatusCode(), UserApiException.INTERNAL_SERVER_ERROR.getStatusMessage());
                        message.reply(Json.encodePrettily(result.cause()), new DeliveryOptions().addHeader("content-type", "application/json; charset=urf-8"));
                    }
                });
            } catch (Exception e) {
                logUnexpectedError("POST_user", e);
                message.fail(MainApiException.INTERNAL_SERVER_ERROR.getStatusCode(), MainApiException.INTERNAL_SERVER_ERROR.getStatusMessage());
            }
        });
        
    }
    
    private void manageError(Message<JsonObject> message, Throwable cause, String serviceName) {
        int code = MainApiException.INTERNAL_SERVER_ERROR.getStatusCode();
        String statusMessage = MainApiException.INTERNAL_SERVER_ERROR.getStatusMessage();
        if (cause instanceof MainApiException) {
            code = ((MainApiException)cause).getStatusCode();
            statusMessage = ((MainApiException)cause).getStatusMessage();
        } else {
            logUnexpectedError(serviceName, cause); 
        }
            
        message.fail(code, statusMessage);
    }
    
    private void logUnexpectedError(String serviceName, Throwable cause) {
        LOGGER.error("Unexpected error in "+ serviceName, cause);
    }
}
