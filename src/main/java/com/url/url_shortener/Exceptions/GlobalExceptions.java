package com.url.url_shortener.Exceptions;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class GlobalExceptions {

    @ExceptionHandler(ResourceNotFound.class)
    public ResponseEntity<Object> handleResourceNotFound(ResourceNotFound exception, WebRequest request) {
        Map<String, Object> resBody = new HashMap<>();
        resBody.put("timestamp", new Date());
        resBody.put("status", HttpStatus.NOT_FOUND.value());
        resBody.put("error", "Not Found");
        resBody.put("message", exception.getMessage());
        resBody.put("path", request.getDescription(false));
        return new ResponseEntity<>(resBody, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvalidRequest.class)
    public ResponseEntity<Object> handleInvalidRequest(InvalidRequest exception, WebRequest request) {
        Map<String, Object> resBody = new HashMap<>();
        resBody.put("timestamp", new Date());
        resBody.put("status", HttpStatus.BAD_REQUEST.value());
        resBody.put("error", "Bad Request");
        resBody.put("message", exception.getMessage());
        resBody.put("path", request.getDescription(false));
        return new ResponseEntity<>(resBody, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Object> handleUnauthorizedException(UnauthorizedException exception, WebRequest request) {
        Map<String, Object> resBody = new HashMap<>();
        resBody.put("timestamp", new Date());
        resBody.put("status", HttpStatus.UNAUTHORIZED.value());
        resBody.put("error", "Unauthorized");
        resBody.put("message", exception.getMessage());
        resBody.put("path", request.getDescription(false));
        return new ResponseEntity<>(resBody, HttpStatus.UNAUTHORIZED);
    }
}
