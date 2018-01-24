package io.swagger.server.api.verticle;

import io.swagger.server.api.MainApiException;

public final class UserApiException extends MainApiException {
    public UserApiException(int statusCode, String statusMessage) {
        super(statusCode, statusMessage);
    }
    
    

}