package Projet.flow.dao;

import Projet.flow.model.Devise;
import Projet.flow.util.Session;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DeviseDAOImpl extends AbstractDAO implements DeviseDAO {

    public DeviseDAOImpl(Session session) {
        super(session);
    }

    private Devise convertirEnDevise(ResultSet rs) throws SQLException {
        return new Devise(rs.getInt("id_devise"), rs.getString("code"));
    }

    // Codes ISO 4217 : toujours en majuscules ("eur" -> "EUR")
    private static String normaliser(String code) {
        return code == null ? null : code.trim().toUpperCase(Locale.ROOT);
    }

    @Override
    public int creer(Devise devise) throws SQLException {

        String sql = "INSERT INTO DEVISE (code) VALUES (?)";

        try (Connection conn = connexion();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, normaliser(devise.getCode()));
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    devise.setIdDevise(id);
                    return id;
                }
            }
            throw new SQLException("ID de la devise non généré");

        } catch (SQLException e) {
            throw traduire(e, "Cette devise existe déjà", null);
        }
    }

    @Override
    public Devise trouverParId(int idDevise) throws SQLException {

        String sql = "SELECT id_devise, code FROM DEVISE WHERE id_devise = ?";

        try (Connection conn = connexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idDevise);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? convertirEnDevise(rs) : null;
            }
        } catch (SQLException e) {
            throw traduire(e, null, null);
        }
    }

    @Override
    public Devise trouverParCode(String code) throws SQLException {

        String sql = "SELECT id_devise, code FROM DEVISE WHERE code = ?";

        try (Connection conn = connexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, normaliser(code));
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? convertirEnDevise(rs) : null;
            }
        } catch (SQLException e) {
            throw traduire(e, null, null);
        }
    }

    @Override
    public List<Devise> listerToutes() throws SQLException {

        String sql = "SELECT id_devise, code FROM DEVISE ORDER BY code";
        List<Devise> devises = new ArrayList<>();

        try (Connection conn = connexion();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                devises.add(convertirEnDevise(rs));
            }
        } catch (SQLException e) {
            throw traduire(e, null, null);
        }
        return devises;
    }

    @Override
    public boolean modifier(Devise devise) throws SQLException {

        String sql = "UPDATE DEVISE SET code = ? WHERE id_devise = ?";

        try (Connection conn = connexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, normaliser(devise.getCode()));
            stmt.setInt(2, devise.getIdDevise());
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw traduire(e, "Cette devise existe déjà", null);
        }
    }

    @Override
    public boolean supprimer(int idDevise) throws SQLException {

        String sql = "DELETE FROM DEVISE WHERE id_devise = ?";

        try (Connection conn = connexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idDevise);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw traduire(e, null, "Impossible : cette devise est utilisée par des comptes");
        }
    }
}