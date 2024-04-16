package com.ides.users;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class UsersLambdaHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

	private static final String DB_URL = "jdbc:postgresql://host.docker.internal:5432/mydatabase";
    private static final String USERNAME = "postgres"; 
    private static final String PASSWORD = "mysecretpassword"; 


    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            User user = mapper.readValue(request.getBody(), User.class);

            // Hash the password before storing it in the database
            String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
            user.setPassword(hashedPassword);

            insertUserData(user);
            return createResponse("User inserted successfully", 200);
        } catch (Exception e) {
            context.getLogger().log("Error processing request: " + e.getMessage());
            return createResponse("Error processing request", 500);
        }
    }

    private void insertUserData(User user) throws Exception {
        try (Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD)) {
            String sql = "INSERT INTO users (userName, firstName, lastName, emailAddress, password) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.setString(1, user.getUserName());
                statement.setString(2, user.getFirstName());
                statement.setString(3, user.getLastName());
                statement.setString(4, user.getEmailAddress());
                statement.setString(5, user.getPassword());
                statement.executeUpdate();
            }
        }
    }

    private APIGatewayProxyResponseEvent createResponse(String body, int statusCode) {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setStatusCode(statusCode);
        response.setBody(body);
        return response;
    }

    static class User {
    	
        private String userName;
        private String firstName;
        private String lastName;
        private String emailAddress;
        private String password;

        public String getUserName() { return userName; }
        public void setUserName(String userName) { this.userName = userName; }
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        public String getEmailAddress() { return emailAddress; }
        public void setEmailAddress(String emailAddress) { this.emailAddress = emailAddress; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}

