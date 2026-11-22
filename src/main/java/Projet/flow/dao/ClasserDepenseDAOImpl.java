package Projet.flow.dao;

import Projet.flow.model.ClasserDepense;
import Projet.flow.util.Session;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClasserDepenseDAOImpl extends AbstractDAO implements ClasserDepenseDAO {

    public ClasserDepenseDAOImpl(Session session) {
        super(session);
    }

    private ClasserDepense convertirEnAffectation(ResultSet rs) throws SQLException {
        return new ClasserDepense(
                rs.getInt("id_transaction"),
                rs.getInt("id_categorie"),
                rs.getBigDecimal("montant_affecte")
        );
    }

    @Override
    public void affecter(ClasserDepense affectation) throws SQLException {

        String sql = "{CALL affecter_categorie(?, ?, ?)}";

        try (Connection conn = connexion();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setInt(1, affectation.getIdTransaction());
            stmt.setInt(2, affectation.getIdCategorie());
            stmt.setBigDecimal(3, affectation.getMontantAffecte());
            stmt.execute();

        } catch (SQLException e) {
            throw traduire(e, "Cette catégorie est déjà affectée à cette dépense", null);
        }
    }

    @Override
    public void modifierAffectation(ClasserDepense affectation) throws SQLException {

        String sql = "{CALL modifier_affectation(?, ?, ?)}";

        try (Connection conn = connexion();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setInt(1, affectation.getIdTransaction());
            stmt.setInt(2, affectation.getIdCategorie());
            stmt.setBigDecimal(3, affectation.getMontantAffecte());
            stmt.execute();

        } catch (SQLException e) {
            throw traduire(e, null, null);
        }
    }

    @Override
    public void supprimerAffectation(int idTransaction, int idCategorie) throws SQLException {

        String sql = "{CALL supprimer_affectation(?, ?)}";

        try (Connection conn = connexion();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setInt(1, idTransaction);
            stmt.setInt(2, idCategorie);
            stmt.execute();

        } catch (SQLException e) {
            throw traduire(e, null, null);
        }
    }

    @Override
    public List<ClasserDepense> listerParTransaction(int idTransaction) throws SQLException {

        String sql = """
                SELECT id_transaction, id_categorie, montant_affecte
                FROM v_mes_ventilations
                WHERE id_transaction = ?
                ORDER BY montant_affecte DESC
                """;

        List<ClasserDepense> affectations = new ArrayList<>();

        try (Connection conn = connexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idTransaction);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    affectations.add(convertirEnAffectation(rs));
                }
            }
        } catch (SQLException e) {
            throw traduire(e, null, null);
        }
        return affectations;
    }

    @Override
    public BigDecimal calculerTotalAffecte(int idTransaction) throws SQLException {

        String sql = """
                SELECT COALESCE(SUM(montant_affecte), 0) AS total
                FROM v_mes_ventilations
                WHERE id_transaction = ?
                """;

        try (Connection conn = connexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idTransaction);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next(); // un agrégat renvoie toujours une ligne
                return rs.getBigDecimal("total");
            }
        } catch (SQLException e) {
            throw traduire(e, null, null);
        }
    }
}