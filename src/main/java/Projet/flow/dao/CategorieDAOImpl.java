package Projet.flow.dao;

import Projet.flow.model.Categorie;
import Projet.flow.util.Session;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategorieDAOImpl extends AbstractDAO implements CategorieDAO {

    public CategorieDAOImpl(Session session) {
        super(session);
    }

    private Categorie convertirEnCategorie(ResultSet rs) throws SQLException {
        return new Categorie(rs.getInt("id_categorie"), rs.getString("nom"));
    }

    @Override
    public int creer(Categorie categorie) throws SQLException {

        String sql = "INSERT INTO CATEGORIE (nom) VALUES (?)";

        try (Connection conn = connexion();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, categorie.getNom());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    categorie.setIdCategorie(id);
                    return id;
                }
            }
            throw new SQLException("ID de la catégorie non généré");

        } catch (SQLException e) {
            throw traduire(e, "Cette catégorie existe déjà", null);
        }
    }

    @Override
    public Categorie trouverParId(int idCategorie) throws SQLException {

        String sql = "SELECT id_categorie, nom FROM CATEGORIE WHERE id_categorie = ?";

        try (Connection conn = connexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idCategorie);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? convertirEnCategorie(rs) : null;
            }
        } catch (SQLException e) {
            throw traduire(e, null, null);
        }
    }

    @Override
    public List<Categorie> listerToutes() throws SQLException {

        String sql = "SELECT id_categorie, nom FROM CATEGORIE ORDER BY nom";
        List<Categorie> categories = new ArrayList<>();

        try (Connection conn = connexion();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                categories.add(convertirEnCategorie(rs));
            }
        } catch (SQLException e) {
            throw traduire(e, null, null);
        }
        return categories;
    }

    @Override
    public boolean modifier(Categorie categorie) throws SQLException {

        String sql = "UPDATE CATEGORIE SET nom = ? WHERE id_categorie = ?";

        try (Connection conn = connexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, categorie.getNom());
            stmt.setInt(2, categorie.getIdCategorie());
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw traduire(e, "Cette catégorie existe déjà", null);
        }
    }

    @Override
    public boolean supprimer(int idCategorie) throws SQLException {

        String sql = "DELETE FROM CATEGORIE WHERE id_categorie = ?";

        try (Connection conn = connexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idCategorie);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw traduire(e, null, "Impossible : cette catégorie est utilisée par des dépenses");
        }
    }
}