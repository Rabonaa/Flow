package Projet.flow.util;

import io.github.cdimascio.dotenv.Dotenv;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;

/**
 * Ouvre les connexions MySQL avec le compte correspondant au rôle
 * (moindre privilège) et positionne l'utilisateur courant en session.
 */
public final class ConnectionManager {

    // ignoreIfMissing : pas de plantage si .env est absent
    // (les variables d'environnement système sont alors utilisées)
    private static final Dotenv ENV = Dotenv.configure().ignoreIfMissing().load();

    private ConnectionManager() {}

    /** Connexion pour le login : ce compte ne peut appeler que lire_identifiants. */
    public static Connection getAuthConnection() throws SQLException {
        return DriverManager.getConnection(
                lire("DB_URL"), lire("DB_AUTH_USER"), lire("DB_AUTH_PASSWORD"));
    }

    /** Connexion d'un utilisateur connecté, avec le compte MySQL de son rôle. */
    public static Connection getConnection(Session session) throws SQLException {
        Objects.requireNonNull(session, "Aucune session : il faut se connecter d'abord");

        String prefixe = session.estAdmin() ? "DB_ADMIN" : "DB_APP";
        Connection conn = DriverManager.getConnection(
                lire("DB_URL"), lire(prefixe + "_USER"), lire(prefixe + "_PASSWORD"));

        // Les vues filtrent sur cette variable : sans elle, l'utilisateur ne voit rien
        try (PreparedStatement stmt = conn.prepareStatement("SET @id_utilisateur = ?")) {
            stmt.setInt(1, session.idUtilisateur());
            stmt.execute();
        } catch (SQLException e) {
            conn.close();
            throw e;
        }
        return conn;
    }

    private static String lire(String cle) throws SQLException {
        String valeur = ENV.get(cle);
        if (valeur == null || valeur.isBlank()) {
            throw new SQLException("Variable de connexion manquante : " + cle);
        }
        return valeur;
    }
}