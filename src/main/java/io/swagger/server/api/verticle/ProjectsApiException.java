package io.swagger.server.api.verticle;

import io.swagger.server.api.MainApiException;
import io.swagger.server.api.model.Project;
import java.util.UUID;

public final class ProjectsApiException extends MainApiException {
    public ProjectsApiException(int statusCode, String statusMessage) {
        super(statusCode, statusMessage);
    }

    public ProjectsApiException(int statusCode, String statusMessage, Throwable cause) {
        super(statusCode, statusMessage, cause);
    }
    
    public static final ProjectsApiException INVALID_PROJECT_INPUT_EXCEPTION = new ProjectsApiException(400, "Invalid Project input");
    public static final ProjectsApiException PROJECT_NOT_FOUND_EXCEPTION = new ProjectsApiException(404, "Project Not Found");
    

}