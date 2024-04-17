package com.ides.login;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.mindrot.jbcrypt.BCrypt;

import java.security.Key;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;

public class LoginLambdaHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final String DB_URL = "jdbc:postgresql://host.docker.internal:5432/mydatabase";
    private static final String USERNAME = "postgres"; 
    private static final String PASSWORD = "mysecretpassword"; 
    private static final Key SECRET_KEY = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256);

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            User user = mapper.readValue(request.getBody(), User.class);
            if (authenticateUser(user.getUserName(), user.getPassword())) {
                String jwtToken = generateJWT(user.getUserName());
                return createResponse(jwtToken, 200);
            } else {
                return createResponse("Invalid username or password", 401);
            }
        } catch (Exception e) {
            context.getLogger().log("Error in user login: " + e.getMessage());
            return createResponse("Error processing request", 500);
        }
    }

    private boolean authenticateUser(String userName, String password) throws Exception {
        Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
        String sql = "SELECT password FROM users WHERE userName = ?";
        PreparedStatement statement = conn.prepareStatement(sql);
        statement.setString(1, userName);
        ResultSet resultSet = statement.executeQuery();
        
        if (resultSet.next()) {
            String storedPassword = resultSet.getString("password");
            return BCrypt.checkpw(password, storedPassword);
        }
        resultSet.close();
        statement.close();
        conn.close();
        return false;
    }

    private String generateJWT(String userName) {
        return Jwts.builder()
                .setSubject(userName)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // Token expires in 1 day
                .signWith(SECRET_KEY)
                .compact();
    }

    private APIGatewayProxyResponseEvent createResponse(String body, int statusCode) {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setStatusCode(statusCode);
        response.setBody(body);
        return response;
    }

    static class User {
        private String userName;
        private String password;

        // Getters and setters
        public String getUserName() { return userName; }
        public void setUserName(String userName) { this.userName = userName; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}

