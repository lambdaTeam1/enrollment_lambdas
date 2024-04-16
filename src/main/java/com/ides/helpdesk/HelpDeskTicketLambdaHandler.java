package com.ides.helpdesk;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Timestamp;

public class HelpDeskTicketLambdaHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

	private static final String DB_URL = "jdbc:postgresql://host.docker.internal:5432/mydatabase";
    private static final String USERNAME = "postgres"; 
    private static final String PASSWORD = "mysecretpassword"; 


    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            HelpDeskTicket ticket = mapper.readValue(request.getBody(), HelpDeskTicket.class);

            insertTicketData(ticket);
            return createResponse("Help Desk Ticket created successfully", 200);
        } catch (Exception e) {
            context.getLogger().log("Error processing request: " + e.getMessage());
            return createResponse("Error processing request", 500);
        }
    }

    private void insertTicketData(HelpDeskTicket ticket) throws Exception {
        try (Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD)) {
            String sql = "INSERT INTO help_desk_ticket (first_name, last_name, email_address, country, user_type, topic, support_request, created_tstamp, updated_tstamp) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.setString(1, ticket.getFirstName());
                statement.setString(2, ticket.getLastName());
                statement.setString(3, ticket.getEmailAddress());
                statement.setString(4, ticket.getCountry());
                statement.setString(5, ticket.getUserType());
                statement.setString(6, ticket.getTopic());
                statement.setString(7, ticket.getSupportRequest());
                statement.setTimestamp(8, new Timestamp(System.currentTimeMillis()));  
                statement.setTimestamp(9, new Timestamp(System.currentTimeMillis())); 
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

    static class HelpDeskTicket {
        private String firstName;
        private String lastName;
        private String emailAddress;
        private String country;
        private String userType;
        private String topic;
        private String supportRequest;

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public String getEmailAddress() {
            return emailAddress;
        }

        public void setEmailAddress(String emailAddress) {
            this.emailAddress = emailAddress;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public String getUserType() {
            return userType;
        }

        public void setUserType(String userType) {
            this.userType = userType;
        }

        public String getTopic() {
            return topic;
        }

        public void setTopic(String topic) {
            this.topic = topic;
        }

        public String getSupportRequest() {
            return supportRequest;
        }

        public void setSupportRequest(String supportRequest) {
            this.supportRequest = supportRequest;
        }
    }

}
