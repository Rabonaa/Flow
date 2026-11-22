package Projet.flow.dao;

import Projet.flow.model.DepenseCategorie;
import Projet.flow.model.LigneTableauBord;
import Projet.flow.util.Session;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TableauBordDAOImpl extends AbstractDAO implements TableauBordDAO {

    public TableauBordDAOImpl(Session session) {
        super(session);
    }

    @Override
    public List<LigneTableauBord> lireTableauBord(LocalDate depuis) throws SQLException {

        String sql = """
                SELECT mois, devise, type, rubrique, total, nb_operations, rafraichi_le
                FROM v_mon_tableau_bord
                WHERE mois >= ?
                ORDER BY mois DESC, type DESC, total DESC
                """;

        List<LigneTableauBord> lignes = new ArrayList<>();

        try (Connection conn = connexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(depuis));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lignes.add(new LigneTableauBord(
                            rs.getDate("mois").toLocalDate(),
                            rs.getString("devise"),
                            rs.getString("type"),
                            rs.getString("rubrique"),
                            rs.getBigDecimal("total"),
                            rs.getInt("nb_operations"),
                            rs.getTimestamp("rafraichi_le").toLocalDateTime()
                    ));
                }
            }
        } catch (SQLException e) {
            throw traduire(e, null, null);
        }
        return lignes;
    }

    @Override
    public List<DepenseCategorie> depensesParCategorie(int nbMois) throws SQLException {

        String sql = """
                SELECT cat.nom AS categorie,
                       COUNT(DISTINCT v.id_transaction) AS nb_depenses,
                       SUM(v.montant_affecte) AS total,
                       ROUND(100 * SUM(v.montant_affecte) / (
                           SELECT SUM(v2.montant_affecte)
                           FROM v_mes_ventilations v2
                           JOIN v_mes_transactions t2 ON t2.id_transaction = v2.id_transaction
                           WHERE t2.date_transaction >= DATE_SUB(CURRENT_DATE, INTERVAL ? MONTH)
                       ), 1) AS part_pourcent
                FROM v_mes_ventilations v
                JOIN v_mes_transactions t ON t.id_transaction = v.id_transaction
                JOIN CATEGORIE cat ON cat.id_categorie = v.id_categorie
                WHERE t.date_transaction >= DATE_SUB(CURRENT_DATE, INTERVAL ? MONTH)
                GROUP BY cat.nom
                ORDER BY total DESC
                """;

        List<DepenseCategorie> lignes = new ArrayList<>();

        try (Connection conn = connexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, nbMois);
            stmt.setInt(2, nbMois);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lignes.add(new DepenseCategorie(
                            rs.getString("categorie"),
                            rs.getInt("nb_depenses"),
                            rs.getBigDecimal("total"),
                            rs.getBigDecimal("part_pourcent")
                    ));
                }
            }
        } catch (SQLException e) {
            throw traduire(e, null, null);
        }
        return lignes;
    }
}