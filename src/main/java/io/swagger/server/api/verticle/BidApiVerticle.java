package io.swagger.server.api.verticle;

import io.swagger.server.api.MainApiException;
import io.swagger.server.api.model.Bid;
import io.swagger.server.api.model.Project;
import io.swagger.server.api.service.BidApi;
import io.swagger.server.api.service.ProjectsApi;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.mongo.MongoClient;

public class BidApiVerticle extends AbstractVerticle {
    final static Logger LOGGER = LoggerFactory.getLogger(BidApiVerticle.class);

    final static String POST_BIDS_SERVICE_ID = "POST_bids";
    final static String PUT_BIDS_SERVICE_ID = "PUT_bids";

    MongoClient mongo;

    final BidApi service;
    final ProjectsApi projectsApiService;

    public BidApiVerticle() {
        try {
            Class serviceImplClass = getClass().getClassLoader().loadClass("io.swagger.server.api.verticle.BidApiImpl");
            Class projectsApiImplClass = getClass().getClassLoader().loadClass("io.swagger.server.api.verticle.ProjectsApiImpl");
            service = (BidApi)serviceImplClass.newInstance();
            projectsApiService = (ProjectsApi)projectsApiImplClass.newInstance();
        } catch (Exception e) {
            logUnexpectedError("BidApiVerticle constructor", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void start() throws Exception {

        mongo = MongoClient.createShared(vertx, config());

        //Consumer for POST_bids
        vertx.eventBus().<JsonObject> consumer(POST_BIDS_SERVICE_ID).handler(message -> {
            try {
                Bid bid = Json.mapper.readValue(message.body().getJsonObject("bid").encode(), Bid.class);

                String projectId = bid.getProjectId();

                projectsApiService.getProjectById(mongo, projectId, result -> {
                    if (result.succeeded()) {
                        if (result.result() != null && !result.result().isEmpty()) {
                            Project project = new Project(result.result());
                            service.bidsPost(mongo, project, bid, updateResult -> {
                                if (updateResult.succeeded()) {
                                    projectsApiService.updateToLowestBidAmount(mongo, project);
                                    message.reply(new JsonObject(Json.encode(updateResult.result())).encodePrettily(),
                                            new DeliveryOptions().addHeader("content-type", "application/json; charset=urf-8"));
                                } else {
                                    Throwable cause = updateResult.cause();
                                    manageError(message, cause, "Bid on project");
                                    message.fail(BidApiException.INTERNAL_SERVER_ERROR.getStatusCode(), BidApiException.INTERNAL_SERVER_ERROR.getStatusMessage());
                                    message.reply(updateResult.cause(), new DeliveryOptions().addHeader("content-type", "application/json; charset=urf-8"));
                                }
                            });
                        } else {
                            message.fail(ProjectsApiException.PROJECT_NOT_FOUND_EXCEPTION.getStatusCode(), ProjectsApiException.PROJECT_NOT_FOUND_EXCEPTION.getStatusMessage());
                            message.reply(result.cause(), new DeliveryOptions().addHeader("content-type", "application/json; charset=urf-8"));
                        }
                    }
                });
            } catch (Exception e) {
                logUnexpectedError("Bid on Project", e);
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
