package Projet.flow.util;

import io.github.cdimascio.dotenv.Dotenv;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionManager {

    // Chargement du fichier .env
    private static final Dotenv dotenv = Dotenv.load();

    // Récupération des identifiants
    private static final String DB_URL = dotenv.get("DB_URL");
    private static final String DB_USER = dotenv.get("DB_USER");
    private static final String DB_PASSWORD = dotenv.get("DB_PASSWORD");

    private ConnectionManager() {}

    public static Connection getConnection() throws SQLException {

        if (DB_URL == null || DB_USER == null || DB_PASSWORD == null) {
            throw new SQLException("Variables de connexion manquantes");
        }

        return DriverManager.getConnection(
                DB_URL,
                DB_USER,
                DB_PASSWORD
        );
    }
}