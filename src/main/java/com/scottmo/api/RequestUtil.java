package com.scottmo.api;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

public class RequestUtil {
    public static ResponseEntity<Map<String, Object>> errorResponse(String message, Throwable e) {
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

    public static ResponseEntity<Map<String, Object>> errorResponse(String message) {
        return errorResponse(message, null);
    }

    public static ResponseEntity<Map<String, Object>> successResponse(Object data) {
        Map<String, Object> response = new HashMap<>();

        response.put("status", "ok");

        if (data != null) {
            response.put("data", data);
        }

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public static ResponseEntity<Map<String, Object>> successResponse() {
        return successResponse(null);
    }

    public static ResponseEntity<Resource> download(Path filePath) throws MalformedURLException {
        Resource resource = new UrlResource(filePath.toUri());

        if (resource.exists() && resource.isReadable()) {
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filePath.getFileName().toString() + "\"")
                    .body(resource);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
