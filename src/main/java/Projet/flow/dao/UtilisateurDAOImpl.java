
package Projet.flow.dao;

import Projet.flow.model.Utilisateur;
import Projet.flow.util.ConnectionManager;

import java.sql.*;
import java.time.LocalDate;

public class UtilisateurDAOImpl implements UtilisateurDAO {

    private Utilisateur convertirEnUtilisateur(ResultSet rs)
            throws SQLException {

        Date dateSQL = rs.getDate("date_inscription");
        LocalDate dateInscription = dateSQL != null
                ? dateSQL.toLocalDate()
                : null;

        return new Utilisateur(
                rs.getInt("id_utilisateur"),
                rs.getString("role"),
                rs.getString("pseudo"),
                rs.getString("email"),
                rs.getString("mdp"),
                dateInscription
        );
    }


    @Override
    public int creer(Utilisateur utilisateur) throws SQLException {

        String sql = """
                INSERT INTO UTILISATEUR
                (role, pseudo, email, mdp, date_inscription)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, utilisateur.getRole());
            stmt.setString(2, utilisateur.getPseudo());
            stmt.setString(3, utilisateur.getEmail());
            stmt.setString(4, utilisateur.getMdp());

            LocalDate date = utilisateur.getDateInscription();
            stmt.setDate(5, date != null ? Date.valueOf(date) : null);

            int lignes = stmt.executeUpdate();

            if (lignes != 1) {
                throw new SQLException("Création de l'utilisateur échouée");
            }

            try (ResultSet rs = stmt.getGeneratedKeys()) {

                if (rs.next()) {
                    int id = rs.getInt(1);
                    utilisateur.setIdUtilisateur(id);
                    return id;
                }
            }

            throw new SQLException("ID utilisateur non généré");
        }
    }


    @Override
    public Utilisateur trouverParId(int idUtilisateur)
            throws SQLException {

        String sql = """
                SELECT *
                FROM UTILISATEUR
                WHERE id_utilisateur = ?
                """;

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUtilisateur);

            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {
                    return convertirEnUtilisateur(rs);
                }
            }
        }

        return null;
    }


    @Override
    public Utilisateur trouverParEmail(String email)
            throws SQLException {

        String sql = """
                SELECT *
                FROM UTILISATEUR
                WHERE email = ?
                """;

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {
                    return convertirEnUtilisateur(rs);
                }
            }
        }

        return null;
    }


    @Override
    public boolean modifier(Utilisateur utilisateur)
            throws SQLException {

        String sql = """
                UPDATE UTILISATEUR
                SET role = ?,
                    pseudo = ?,
                    email = ?,
                    mdp = ?,
                    date_inscription = ?
                WHERE id_utilisateur = ?
                """;

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, utilisateur.getRole());
            stmt.setString(2, utilisateur.getPseudo());
            stmt.setString(3, utilisateur.getEmail());
            stmt.setString(4, utilisateur.getMdp());

            LocalDate date = utilisateur.getDateInscription();
            stmt.setDate(5, date != null ? Date.valueOf(date) : null);

            stmt.setInt(6, utilisateur.getIdUtilisateur());

            return stmt.executeUpdate() > 0;
        }
    }


    @Override
    public boolean supprimer(int idUtilisateur)
            throws SQLException {

        String sql = """
                DELETE FROM UTILISATEUR
                WHERE id_utilisateur = ?
                """;

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUtilisateur);

            return stmt.executeUpdate() > 0;
        }
    }
}
