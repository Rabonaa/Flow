package Projet.flow.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionManager {
    private static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/?user=user";
    private static final String DB_USER = "user";
    private static final String DB_PASSWORD = "Password123";

    private ConnectionManager(){}

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }
}
