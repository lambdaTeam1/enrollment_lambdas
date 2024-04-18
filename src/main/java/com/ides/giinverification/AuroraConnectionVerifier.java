package com.ides.giinverification;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class AuroraConnectionVerifier implements RequestHandler<Object, String> {

//	test db credentials
  private static final String DB_URL = "jdbc:postgresql://host.docker.internal:5432/mydatabase";
  private static final String USERNAME = "postgres"; 
  private static final String PASSWORD = "mysecretpassword"; 
	
//  prod credentials
//  private static final String DB_URL = "jdbc:postgresql://facta-ides-instance-1.citapqw7l8pf.us-gov-west-1.rds.amazonaws.com:5432/postgres";
//  private static final String USERNAME = "postgres"; 
//  private static final String PASSWORD = "Jut545krby"; 

    @Override
    public String handleRequest(Object input, Context context) {
        try (Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD)) {

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT 1");

            if (rs.next()) {
                return "Aurora connection successful!";
            } else {
                return "Aurora connection failed!";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Aurora connection failed: " + e.getMessage();
        }
    }
}
