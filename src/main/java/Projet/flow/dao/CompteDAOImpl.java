package Projet.flow.dao;

import Projet.flow.model.Compte;
import Projet.flow.util.Session;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CompteDAOImpl extends AbstractDAO implements CompteDAO {

    private static final String COLONNES =
            "id_compte, nom, type, montant_initial, id_devise, id_utilisateur";

    public CompteDAOImpl(Session session) {
        super(session);
    }

    private Compte convertirEnCompte(ResultSet rs) throws SQLException {
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

        // WITH CHECK OPTION sur la vue refuse tout id_utilisateur différent de la session
        String sql = """
                INSERT INTO v_mes_comptes (nom, type, montant_initial, id_devise, id_utilisateur)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection conn = connexion();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, compte.getNom());
            stmt.setString(2, compte.getType());
            stmt.setBigDecimal(3, compte.getMontantInitial() != null
                    ? compte.getMontantInitial() : BigDecimal.ZERO);
            stmt.setInt(4, compte.getIdDevise());
            stmt.setInt(5, session.idUtilisateur());

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    compte.setIdCompte(id);
                    compte.setIdUtilisateur(session.idUtilisateur());
                    return id;
                }
            }
            throw new SQLException("ID du compte non généré");

        } catch (SQLException e) {
            throw traduire(e, null, null);
        }
    }

    @Override
    public Compte trouverParId(int idCompte) throws SQLException {

        String sql = "SELECT " + COLONNES + " FROM v_mes_comptes WHERE id_compte = ?";

        try (Connection conn = connexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idCompte);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? convertirEnCompte(rs) : null;
            }
        } catch (SQLException e) {
            throw traduire(e, null, null);
        }
    }

    @Override
    public List<Compte> listerMesComptes() throws SQLException {

        String sql = "SELECT " + COLONNES + " FROM v_mes_comptes ORDER BY id_compte";
        List<Compte> comptes = new ArrayList<>();

        try (Connection conn = connexion();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                comptes.add(convertirEnCompte(rs));
            }
        } catch (SQLException e) {
            throw traduire(e, null, null);
        }
        return comptes;
    }

    @Override
    public boolean modifier(Compte compte) throws SQLException {

        String sql = """
                UPDATE v_mes_comptes
                SET nom = ?, type = ?, montant_initial = ?, id_devise = ?
                WHERE id_compte = ?
                """;

        try (Connection conn = connexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, compte.getNom());
            stmt.setString(2, compte.getType());
            stmt.setBigDecimal(3, compte.getMontantInitial());
            stmt.setInt(4, compte.getIdDevise());
            stmt.setInt(5, compte.getIdCompte());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw traduire(e, null, null);
        }
    }

    @Override
    public boolean supprimer(int idCompte) throws SQLException {

        String sql = "DELETE FROM v_mes_comptes WHERE id_compte = ?";

        try (Connection conn = connexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idCompte);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw traduire(e, null, "Impossible : ce compte contient des transactions");
        }
    }

    @Override
    public BigDecimal calculerSolde(int idCompte) throws SQLException {

        String sql = "SELECT calculer_solde_compte(?) AS solde";

        try (Connection conn = connexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idCompte);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal("solde");
                }
            }
            throw new SQLException("Impossible de calculer le solde");

        } catch (SQLException e) {
            throw traduire(e, null, null);
        }
    }
}