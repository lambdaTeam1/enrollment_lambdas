package com.ides.giinverification;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class HCTAEnrollmentValidatorLambdaHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final String DB_URL = "jdbc:postgresql://host.docker.internal:5432/mydatabase";
    private static final String USERNAME = "postgres";
    private static final String PASSWORD = "mysecretpassword";

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        String type = "";
        String value = "";

        // Decode and sanitize query parameters
        try {
            type = URLDecoder.decode(request.getQueryStringParameters().get("type"), "UTF-8").trim();
            value = URLDecoder.decode(request.getQueryStringParameters().get("value"), "UTF-8").trim();
        } catch (Exception e) {
            context.getLogger().log("Error decoding query parameters: " + e.getMessage());
            return createResponse("Error decoding query parameters", 400);
        }

        try {
            Map<String, Object> result = verifyValue(type, value);
            if (result != null) {
                return createResponse(result, 200);
            } else {
                return createResponse("Value not found or invalid", 404);
            }
        } catch (Exception e) {
            context.getLogger().log("Error processing request: " + e.getMessage());
            return createResponse("Error processing request", 500);
        }
    }

    private Map<String, Object> verifyValue(String type, String value) throws Exception {
        try (Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD)) {
            String sql;
            if ("hcta-username".equals(type)) {
                sql = "SELECT hcta_username, giin, is_admin_created FROM hcta_giin_mapping WHERE hcta_username = ?";
            } else if ("giin".equals(type)) {
                sql = "SELECT giin_number, type, country FROM giin_records WHERE giin_number = ?";
            } else {
                return null;  // Type is neither "hcta-username" nor "giin"
            }

            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.setString(1, value);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        Map<String, Object> result = new HashMap<>();
                        if ("hcta-username".equals(type)) {
                            result.put("success", true);
                            result.put("message", "Input is valid");
                            result.put("giin", resultSet.getString("giin"));
                            result.put("adminUserExists", resultSet.getBoolean("is_admin_created"));
                        } else if ("giin".equals(type)) {
                            result.put("success", true);
                            result.put("message", "GIIN found");
                            result.put("giin_number", resultSet.getString("giin_number"));
                            result.put("type", resultSet.getString("type"));
                            result.put("country", resultSet.getString("country"));
                            // adminUserExists not applicable for giin type, provide appropriate logic if needed
                        }
                        return result;
                    }
                }
            }
        } catch (SQLException e) {
            throw new Exception("Database error: " + e.getMessage());
        }
        return null;
    }

    private APIGatewayProxyResponseEvent createResponse(Object body, int statusCode) {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setStatusCode(statusCode);
        try {
            String responseBody = body instanceof String ? (String) body : new ObjectMapper().writeValueAsString(body);
            response.setBody(responseBody);
        } catch (JsonProcessingException e) {
            response.setBody("{\"success\": false, \"message\": \"Failed to serialize response\"}");
            response.setStatusCode(500);
        }
        return response;
    }
}


