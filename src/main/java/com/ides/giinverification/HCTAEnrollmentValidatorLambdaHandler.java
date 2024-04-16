package com.ides.giinverification;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

public class HCTAEnrollmentValidatorLambdaHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final String DB_URL = "jdbc:postgresql://host.docker.internal:5432/mydatabase";
    private static final String USERNAME = "postgres"; 
    private static final String PASSWORD = "mysecretpassword"; 

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            EnrollmentRequest enrollmentRequest = mapper.readValue(request.getBody(), EnrollmentRequest.class);
            Map<String, String> result = verifyEnrollment(enrollmentRequest.getHctaUsername());
            if (result != null) {
                return createResponse(mapper.writeValueAsString(result), 200);
            } else {
                return createResponse("HCTA username not found or invalid", 404);
            }
        } catch (Exception e) {
            context.getLogger().log("Error verifying HCTA username: " + e.getMessage());
            return createResponse("Error processing request", 500);
        }
    }

    private Map<String, String> verifyEnrollment(String hctaUsername) throws Exception {
        Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
        String sql = "SELECT hcta_username, giin, is_admin_created FROM hcta_enrollment_info WHERE hcta_username = ?";
        PreparedStatement statement = conn.prepareStatement(sql);
        statement.setString(1, hctaUsername);
        ResultSet resultSet = statement.executeQuery();

        if (resultSet.next()) {
            if (resultSet.getBoolean("is_admin_created")) {
                resultSet.close();
                statement.close();
                conn.close();
                return null;  // Admin already exists for this GIIN
            }

            Map<String, String> result = new HashMap<>();
            result.put("hcta_username", resultSet.getString("hcta_username"));
            result.put("giin", resultSet.getString("giin"));
            resultSet.close();
            statement.close();
            conn.close();
            return result;
        }
        resultSet.close();
        statement.close();
        conn.close();
        return null;
    }

    private APIGatewayProxyResponseEvent createResponse(String body, int statusCode) {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setStatusCode(statusCode);
        response.setBody(body);
        return response;
    }

    static class EnrollmentRequest {
        private String hctaUsername;

        public String getHctaUsername() { return hctaUsername; }
        public void setHctaUsername(String hctaUsername) { this.hctaUsername = hctaUsername; }
    }
}

