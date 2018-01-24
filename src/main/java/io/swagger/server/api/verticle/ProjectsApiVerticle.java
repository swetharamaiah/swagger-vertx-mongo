package io.swagger.server.api.verticle;

import io.swagger.server.api.MainApiException;
import io.swagger.server.api.model.Project;
import io.swagger.server.api.service.ProjectsApi;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.mongo.MongoClient;

public class ProjectsApiVerticle extends AbstractVerticle {
    final static Logger LOGGER = LoggerFactory.getLogger(ProjectsApiVerticle.class);

    final static String ADDPROJECT_SERVICE_ID = "addProject";
    final static String GETPROJECTBYID_SERVICE_ID = "getProjectById";
    final static String GETPROJECTS_SERVICE_ID = "getProjects";
    final static String UPDATEPROJECT_SERVICE_ID = "updateProject";

    MongoClient mongo;

    final ProjectsApi service;

    public ProjectsApiVerticle() {
        try {
            Class serviceImplClass = getClass().getClassLoader().loadClass("io.swagger.server.api.verticle.ProjectsApiImpl");
            service = (ProjectsApi)serviceImplClass.newInstance();
        } catch (Exception e) {
            logUnexpectedError("ProjectsApiVerticle constructor", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void start() throws Exception {

        mongo = MongoClient.createShared(vertx, config());

        //Consumer for addProject
        vertx.eventBus().<JsonObject> consumer(ADDPROJECT_SERVICE_ID).handler(message -> {
            try {
                Project newProject = Json.mapper.readValue(message.body().getJsonObject("newProject").encode(), Project.class);
                service.addProject(mongo, newProject, result -> {
                    if (result.succeeded()) {
                        message.reply(Json.encodePrettily(newProject.setId(result.result())),
                                new DeliveryOptions().addHeader("content-type", "application/json; charset=urf-8"));

                    } else {
                        message.fail(ProjectsApiException.INTERNAL_SERVER_ERROR.getStatusCode(), ProjectsApiException.INTERNAL_SERVER_ERROR.getStatusMessage());
                        message.reply(result.cause(), new DeliveryOptions().addHeader("content-type", "application/json; charset=urf-8"));
                    }
                });
            } catch (Exception e) {
                logUnexpectedError("addProject", e);
                message.fail(MainApiException.INTERNAL_SERVER_ERROR.getStatusCode(), MainApiException.INTERNAL_SERVER_ERROR.getStatusMessage());
            }
        });

        //Consumer for getProjectById
        vertx.eventBus().<JsonObject> consumer(GETPROJECTBYID_SERVICE_ID).handler(message -> {
            try {
                String projectId = message.body().getString("project_id");
                service.getProjectById(mongo, projectId, result -> {
                    if (result.succeeded()) {
                        if (result.result() != null && !result.result().isEmpty()) {
                            message.reply(result.result().encodePrettily(),
                                    new DeliveryOptions().addHeader("content-type", "application/json; charset=urf-8"));
                        } else {
                            message.fail(ProjectsApiException.PROJECT_NOT_FOUND_EXCEPTION.getStatusCode(), ProjectsApiException.PROJECT_NOT_FOUND_EXCEPTION.getStatusMessage());
                            message.reply(result.cause(), new DeliveryOptions().addHeader("content-type", "application/json; charset=urf-8"));
                        }

                    } else {
                        Throwable cause = result.cause();

                        message.fail(ProjectsApiException.PROJECT_NOT_FOUND_EXCEPTION.getStatusCode(), ProjectsApiException.PROJECT_NOT_FOUND_EXCEPTION.getStatusMessage());
                        manageError(message, cause, "getProjectById");
                        message.reply(result.cause(), new DeliveryOptions().addHeader("content-type", "application/json; charset=urf-8"));
                    }
                });
            } catch (Exception e) {
                logUnexpectedError("getProjectById", e);
                message.fail(MainApiException.INTERNAL_SERVER_ERROR.getStatusCode(),
                        MainApiException.INTERNAL_SERVER_ERROR.getStatusMessage());
            }
        });

        //Consumer for getProjects
        vertx.eventBus().<JsonObject> consumer(GETPROJECTS_SERVICE_ID).handler(message -> {
            try {
                String status = message.body().getString("status");
                status = status != null ? status : null;
                service.getProjects(mongo, status, result -> {
                    if (result.succeeded()) {
                        if (result.result() != null && !result.result().isEmpty()) {
                            message.reply(new JsonArray(Json.encode(result.result())).encodePrettily(),
                                    new DeliveryOptions().addHeader("content-type", "application/json; charset=urf-8"));
                        } else {
                            message.fail(ProjectsApiException.PROJECT_NOT_FOUND_EXCEPTION.getStatusCode(), ProjectsApiException.PROJECT_NOT_FOUND_EXCEPTION.getStatusMessage());
                            message.reply(result.cause(), new DeliveryOptions().addHeader("content-type", "application/json; charset=urf-8"));
                        }
                    } else {
                        Throwable cause = result.cause();
                        message.fail(ProjectsApiException.PROJECT_NOT_FOUND_EXCEPTION.getStatusCode(), ProjectsApiException.PROJECT_NOT_FOUND_EXCEPTION.getStatusMessage());
                        manageError(message, cause, "getProjects");
                        message.reply(result.cause(), new DeliveryOptions().addHeader("content-type", "application/json; charset=urf-8"));
                    }
                });
            } catch (Exception e) {
                logUnexpectedError("getProjects", e);
                message.fail(MainApiException.INTERNAL_SERVER_ERROR.getStatusCode(), MainApiException.INTERNAL_SERVER_ERROR.getStatusMessage());
            }
        });

        // Update a project
        //TODO: check name error
        vertx.eventBus().<JsonObject> consumer(UPDATEPROJECT_SERVICE_ID).handler(message -> {
            try {
                String projectId = message.body().getString("project_id");
                JsonObject query = new JsonObject().put("_id", projectId);
                JsonObject update = message.body().getJsonObject("updateProject");
                service.updateProject(mongo, query, update, result -> {
                    if (result.succeeded()) {
                        message.reply(Json.encodePrettily(result.result()),
                                new DeliveryOptions().addHeader("content-type", "application/json; charset=urf-8"));

                    } else {
                        Throwable cause = result.cause();
                        manageError(message, cause, "updateProject");
                    }
                });
            } catch (Exception e) {
                logUnexpectedError("updateProject", e);
                message.fail(MainApiException.INTERNAL_SERVER_ERROR.getStatusCode(), MainApiException.INTERNAL_SERVER_ERROR.getStatusMessage());
            }
        });

        //5 minutes
        vertx.setPeriodic(300000, id -> {
            // This handler will get called every 5 minutes
            LOGGER.info("timer fired!");

            service.closeExpiringProjects(mongo, Project.StatusEnum.CLOSED, updateResult -> {
                if (updateResult.succeeded()) {
                    System.out.println(updateResult.result().getDocMatched());
                    LOGGER.info("Update Async io.swagger.server.api..");
                } else {
                    Throwable cause = updateResult.cause();
                    LOGGER.error("Error in updating Async Job", cause);
                }
            });
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
