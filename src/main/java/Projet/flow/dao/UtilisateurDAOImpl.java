package Projet.flow.dao;

import Projet.flow.model.Utilisateur;
import Projet.flow.util.Session;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class UtilisateurDAOImpl extends AbstractDAO implements UtilisateurDAO {

    // Jamais "mdp" ici : le compte admin n'a pas le droit SELECT sur cette colonne
    private static final String COLONNES =
            "id_utilisateur, role, pseudo, email, date_inscription";

    public UtilisateurDAOImpl(Session session) {
        super(session);
        if (!session.estAdmin()) {
            throw new IllegalStateException("Gestion des utilisateurs réservée à l'administrateur");
        }
    }

    private Utilisateur convertirEnUtilisateur(ResultSet rs) throws SQLException {
        Date dateSQL = rs.getDate("date_inscription");
        return new Utilisateur(
                rs.getInt("id_utilisateur"),
                rs.getString("role"),
                rs.getString("pseudo"),
                rs.getString("email"),
                null,
                dateSQL != null ? dateSQL.toLocalDate() : null
        );
    }

    @Override
    public int creer(Utilisateur utilisateur) throws SQLException {

        String sql = """
                INSERT INTO UTILISATEUR (role, pseudo, email, mdp, date_inscription)
                VALUES (?, ?, ?, ?, COALESCE(?, CURRENT_DATE))
                """;

        try (Connection conn = connexion();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, utilisateur.getRole());
            stmt.setString(2, utilisateur.getPseudo());
            stmt.setString(3, utilisateur.getEmail());
            stmt.setString(4, utilisateur.getMdp());
            LocalDate date = utilisateur.getDateInscription();
            stmt.setDate(5, date != null ? Date.valueOf(date) : null);

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    utilisateur.setIdUtilisateur(id);
                    utilisateur.setMdp(null); // on ne garde pas le hash en mémoire
                    return id;
                }
            }
            throw new SQLException("ID utilisateur non généré");

        } catch (SQLException e) {
            throw traduire(e, "Cet e-mail est déjà utilisé", null);
        }
    }

    @Override
    public Utilisateur trouverParId(int idUtilisateur) throws SQLException {

        String sql = "SELECT " + COLONNES + " FROM UTILISATEUR WHERE id_utilisateur = ?";

        try (Connection conn = connexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUtilisateur);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? convertirEnUtilisateur(rs) : null;
            }
        } catch (SQLException e) {
            throw traduire(e, null, null);
        }
    }

    @Override
    public Utilisateur trouverParEmail(String email) throws SQLException {

        String sql = "SELECT " + COLONNES + " FROM UTILISATEUR WHERE email = ?";

        try (Connection conn = connexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? convertirEnUtilisateur(rs) : null;
            }
        } catch (SQLException e) {
            throw traduire(e, null, null);
        }
    }

    @Override
    public List<Utilisateur> listerTous() throws SQLException {

        String sql = "SELECT " + COLONNES + " FROM UTILISATEUR ORDER BY id_utilisateur";
        List<Utilisateur> utilisateurs = new ArrayList<>();

        try (Connection conn = connexion();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                utilisateurs.add(convertirEnUtilisateur(rs));
            }
        } catch (SQLException e) {
            throw traduire(e, null, null);
        }
        return utilisateurs;
    }

    @Override
    public boolean modifier(Utilisateur utilisateur) throws SQLException {

        String sql = """
                UPDATE UTILISATEUR
                SET role = ?, pseudo = ?, email = ?
                WHERE id_utilisateur = ?
                """;

        try (Connection conn = connexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, utilisateur.getRole());
            stmt.setString(2, utilisateur.getPseudo());
            stmt.setString(3, utilisateur.getEmail());
            stmt.setInt(4, utilisateur.getIdUtilisateur());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw traduire(e, "Cet e-mail est déjà utilisé", null);
        }
    }

    @Override
    public boolean changerMotDePasse(int idUtilisateur, String nouveauHash) throws SQLException {

        String sql = "UPDATE UTILISATEUR SET mdp = ? WHERE id_utilisateur = ?";

        try (Connection conn = connexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nouveauHash);
            stmt.setInt(2, idUtilisateur);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw traduire(e, null, null);
        }
    }

    @Override
    public boolean supprimer(int idUtilisateur) throws SQLException {

        String sql = "DELETE FROM UTILISATEUR WHERE id_utilisateur = ?";

        try (Connection conn = connexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUtilisateur);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw traduire(e, null, "Impossible : cet utilisateur possède encore des comptes");
        }
    }
}