package io.swagger.server.api.verticle;

import io.swagger.server.api.MainApiException;

public final class BidApiException extends MainApiException {
    public BidApiException(int statusCode, String statusMessage) {
        super(statusCode, statusMessage);
    }
    
    

}