package org.BOGO.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Database configuration for MS SQL Server connection.
 */
public class DatabaseConfig {
    
    private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=BOGO;encrypt=false;trustServerCertificate=true;";
    private static final String USER = "sa";
    private static final String PASSWORD = "YourStrong@Pass123";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
