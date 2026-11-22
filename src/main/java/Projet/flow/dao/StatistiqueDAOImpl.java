package Projet.flow.dao;

import Projet.flow.model.StatistiqueMensuelle;
import Projet.flow.util.Session;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StatistiqueDAOImpl extends AbstractDAO implements StatistiqueDAO {

    public StatistiqueDAOImpl(Session session) {
        super(session);
        if (!session.estAdmin()) {
            throw new IllegalStateException("Statistiques réservées à l'administrateur");
        }
    }

    @Override
    public List<StatistiqueMensuelle> statistiquesMensuelles() throws SQLException {

        String sql = """
                SELECT mois, utilisateurs_actifs, nb_transactions, nb_revenus, nb_depenses
                FROM v_admin_statistiques
                ORDER BY mois DESC
                """;

        List<StatistiqueMensuelle> lignes = new ArrayList<>();

        try (Connection conn = connexion();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lignes.add(new StatistiqueMensuelle(
                        rs.getString("mois"),
                        rs.getInt("utilisateurs_actifs"),
                        rs.getInt("nb_transactions"),
                        rs.getInt("nb_revenus"),
                        rs.getInt("nb_depenses")
                ));
            }
        } catch (SQLException e) {
            throw traduire(e, null, null);
        }
        return lignes;
    }

    @Override
    public void rafraichirTableauBord() throws SQLException {

        String sql = "{CALL rafraichir_tableau_bord()}";

        try (Connection conn = connexion();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.execute();

        } catch (SQLException e) {
            throw traduire(e, null, null);
        }
    }
}