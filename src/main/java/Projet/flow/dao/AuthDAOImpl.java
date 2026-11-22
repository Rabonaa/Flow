package Projet.flow.dao;

import Projet.flow.model.Identifiants;
import Projet.flow.util.ConnectionManager;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/** Pas de session ici : on n'est pas encore connecté. */
public class AuthDAOImpl implements AuthDAO {

    @Override
    public Optional<Identifiants> lireIdentifiants(String email) throws SQLException {

        String sql = "{CALL lire_identifiants(?)}";

        try (Connection conn = ConnectionManager.getAuthConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Identifiants(
                            rs.getInt("id_utilisateur"),
                            rs.getString("role"),
                            rs.getString("pseudo"),
                            rs.getString("mdp")
                    ));
                }
            }
        }
        return Optional.empty();
    }
}