package com.ides.giinverification;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

public class GIINVerificationLambdaHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final String DB_URL = "jdbc:postgresql://host.docker.internal:5432/mydatabase";
    private static final String USERNAME = "postgres"; 
    private static final String PASSWORD = "mysecretpassword"; 

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        String giinNumber = request.getPathParameters().get("giin"); // Get giin number from path parameters

        try {
            Map<String, String> result = verifyGIIN(giinNumber);
            if (result != null) {
                return createResponse(result, 200);
            } else {
                return createResponse("GIIN not found", 404);
            }
        } catch (Exception e) {
            context.getLogger().log("Error verifying GIIN: " + e.getMessage());
            return createResponse("Error processing request", 500);
        }
    }

    private Map<String, String> verifyGIIN(String giinNumber) throws Exception {
        Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
        String sql = "SELECT giin_number, type, country FROM giin_records WHERE giin_number = ?";
        PreparedStatement statement = conn.prepareStatement(sql);
        statement.setString(1, giinNumber);
        ResultSet resultSet = statement.executeQuery();

        if (resultSet.next()) {
            Map<String, String> result = new HashMap<>();
            result.put("giin_number", resultSet.getString("giin_number"));
            result.put("type", resultSet.getString("type"));
            result.put("country", resultSet.getString("country"));
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

    private APIGatewayProxyResponseEvent createResponse(Object body, int statusCode) {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setStatusCode(statusCode);
        try {
			response.setBody(body instanceof Map ? new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(body) : (String) body);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return response;
    }
}

