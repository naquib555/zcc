package com.zendesk.zcc.handler;

import com.zendesk.zcc.domain.AppException;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;

@Component
public class ZendeskResponseErrorHandler implements ResponseErrorHandler {
    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        return response.getStatusCode().series() == HttpStatus.Series.CLIENT_ERROR
                || response.getStatusCode().series() == HttpStatus.Series.SERVER_ERROR;
    }

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        if(response.getStatusCode() == HttpStatus.NOT_FOUND)
            throw new AppException("Your searched information does not exists");
        if(response.getStatusCode() == HttpStatus.BAD_REQUEST)
            throw new AppException("You have searched for an Invalid information");
        else if(response.getStatusCode() == HttpStatus.UNAUTHORIZED)
            throw new AppException("Application facing authentication error with service provider");
        else throw new AppException("API is currently unavailable");
    }
}
