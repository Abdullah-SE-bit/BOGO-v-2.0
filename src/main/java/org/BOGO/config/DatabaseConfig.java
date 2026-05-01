package org.BOGO.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class DatabaseConfig {
        private static final String URL = "jdbc:sqlserver://DESKTOP-M0688UR\\SQLEXPRESS;databaseName=bogo;encrypt=true;trustServerCertificate=true;";
        private static final String USER = "BOGO";
        private static final String PASSWORD = "ABD-3740-2006";

        public static Connection getConnection() throws SQLException, SQLException {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        }
}
