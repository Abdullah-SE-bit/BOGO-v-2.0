package org.BOGO;

import org.BOGO.config.DatabaseConfig;
import java.sql.Connection;

public class TestDB {
    public static void main(String[] args) {
        try (Connection conn = DatabaseConfig.getConnection()) {
            System.out.println("Successfully connected to MS SQL Server Database 'BOGO' via Docker!");
        } catch (Exception e) {
            System.err.println("Failed to connect to database!");
            e.printStackTrace();
        }
    }
}
