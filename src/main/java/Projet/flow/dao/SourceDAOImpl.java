package Projet.flow.dao;

import Projet.flow.model.Source;
import Projet.flow.util.Session;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SourceDAOImpl extends AbstractDAO implements SourceDAO {

    public SourceDAOImpl(Session session) {
        super(session);
    }

    private Source convertirEnSource(ResultSet rs) throws SQLException {
        return new Source(rs.getInt("id_source"), rs.getString("nom"));
    }

    @Override
    public int creer(Source source) throws SQLException {

        String sql = "INSERT INTO SOURCE (nom) VALUES (?)";

        try (Connection conn = connexion();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, source.getNom());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    source.setIdSource(id);
                    return id;
                }
            }
            throw new SQLException("ID de la source non généré");

        } catch (SQLException e) {
            throw traduire(e, "Cette source existe déjà", null);
        }
    }

    @Override
    public Source trouverParId(int idSource) throws SQLException {

        String sql = "SELECT id_source, nom FROM SOURCE WHERE id_source = ?";

        try (Connection conn = connexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idSource);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? convertirEnSource(rs) : null;
            }
        } catch (SQLException e) {
            throw traduire(e, null, null);
        }
    }

    @Override
    public List<Source> listerToutes() throws SQLException {

        String sql = "SELECT id_source, nom FROM SOURCE ORDER BY nom";
        List<Source> sources = new ArrayList<>();

        try (Connection conn = connexion();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                sources.add(convertirEnSource(rs));
            }
        } catch (SQLException e) {
            throw traduire(e, null, null);
        }
        return sources;
    }

    @Override
    public boolean modifier(Source source) throws SQLException {

        String sql = "UPDATE SOURCE SET nom = ? WHERE id_source = ?";

        try (Connection conn = connexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, source.getNom());
            stmt.setInt(2, source.getIdSource());
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw traduire(e, "Cette source existe déjà", null);
        }
    }

    @Override
    public boolean supprimer(int idSource) throws SQLException {

        String sql = "DELETE FROM SOURCE WHERE id_source = ?";

        try (Connection conn = connexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idSource);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw traduire(e, null, "Impossible : cette source est utilisée par des revenus");
        }
    }
}