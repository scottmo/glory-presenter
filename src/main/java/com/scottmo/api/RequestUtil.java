package com.scottmo.api;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class RequestUtil {
    public ResponseEntity<Map<String, Object>> errorResponse(String message, Throwable e) {
        Map<String, Object> response = new HashMap<>();

        response.put("status", "error");
        response.put("message", message);

        if (e != null) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            response.put("stacktrace", sw.toString());
        }

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    public ResponseEntity<Map<String, Object>> errorResponse(String message) {
        return errorResponse(message, null);
    }

    public ResponseEntity<Map<String, Object>> successResponse(Object data) {
        Map<String, Object> response = new HashMap<>();

        response.put("status", "ok");

        if (data != null) {
            response.put("data", data);
        }

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public ResponseEntity<Map<String, Object>> successResponse() {
        return successResponse(null);
    }
}
