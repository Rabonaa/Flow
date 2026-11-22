package Projet.flow.dao;

import Projet.flow.model.Compte;
import Projet.flow.util.ConnectionManager;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CompteDAOImpl implements CompteDAO {

    private Compte convertirEnCompte(ResultSet rs)
            throws SQLException {

        return new Compte(
                rs.getInt("id_compte"),
                rs.getString("nom"),
                rs.getString("type"),
                rs.getBigDecimal("montant_initial"),
                rs.getInt("id_devise"),
                rs.getInt("id_utilisateur")
        );
    }

    @Override
    public int creer(Compte compte) throws SQLException {

        String sql = """
                INSERT INTO COMPTE
                (nom, type, montant_initial, id_devise, id_utilisateur)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, compte.getNom());
            stmt.setString(2, compte.getType());
            stmt.setBigDecimal(3, compte.getMontantInitial());
            stmt.setInt(4, compte.getIdDevise());
            stmt.setInt(5, compte.getIdUtilisateur());

            int lignes = stmt.executeUpdate();

            if (lignes != 1) {
                throw new SQLException("Création du compte échouée");
            }

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    compte.setIdCompte(id);
                    return id;
                }
            }

            throw new SQLException("ID du compte non généré");
        }
    }

    @Override
    public Compte trouverParId(int idCompte) throws SQLException {

        String sql = """
                SELECT *
                FROM COMPTE
                WHERE id_compte = ?
                """;

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idCompte);

            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {
                    return convertirEnCompte(rs);
                }
            }
        }

        return null;
    }

    @Override
    public List<Compte> listerParUtilisateur(int idUtilisateur)
            throws SQLException {

        List<Compte> comptes = new ArrayList<>();

        String sql = """
                SELECT *
                FROM COMPTE
                WHERE id_utilisateur = ?
                ORDER BY id_compte
                """;

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUtilisateur);

            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    comptes.add(convertirEnCompte(rs));
                }
            }
        }

        return comptes;
    }

    @Override
    public boolean modifier(Compte compte) throws SQLException {

        String sql = """
                UPDATE COMPTE
                SET nom = ?,
                    type = ?,
                    montant_initial = ?,
                    id_devise = ?
                WHERE id_compte = ?
                """;

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, compte.getNom());
            stmt.setString(2, compte.getType());
            stmt.setBigDecimal(3, compte.getMontantInitial());
            stmt.setInt(4, compte.getIdDevise());
            stmt.setInt(5, compte.getIdCompte());

            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean supprimer(int idCompte) throws SQLException {

        String sql = """
                DELETE FROM COMPTE
                WHERE id_compte = ?
                """;

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idCompte);

            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public BigDecimal calculerSolde(int idCompte)
            throws SQLException {

        String sql = "SELECT calculer_solde_compte(?) AS solde";

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idCompte);

            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {
                    return rs.getBigDecimal("solde");
                }
            }
        }

        throw new SQLException("Impossible de calculer le solde");
    }
}
