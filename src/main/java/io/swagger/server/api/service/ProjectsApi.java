package io.swagger.server.api.service;

import io.swagger.server.api.model.Project;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.mongo.MongoClientUpdateResult;

import java.util.List;

public interface ProjectsApi  {
    //addProject
    MongoClient addProject(MongoClient mongoClient, Project newProject, Handler<AsyncResult<String>> handler);
    
    //getProjectById
    MongoClient getProjectById(MongoClient mongoClient, String projectId, Handler<AsyncResult<JsonObject>> handler);
    
    //getProjects
    MongoClient getProjects(MongoClient mongoClient, String status, Handler<AsyncResult<List<JsonObject>>> handler);

    MongoClient closeExpiringProjects(MongoClient mongoClient, Project.StatusEnum statusEnum,
                                      Handler<AsyncResult<MongoClientUpdateResult>> handler);

    public MongoClient updateProject(MongoClient mongoClient, JsonObject query, JsonObject update, Handler<AsyncResult<MongoClientUpdateResult>> handler);

    void updateToLowestBidAmount(MongoClient mongoClient, Project project);

}
