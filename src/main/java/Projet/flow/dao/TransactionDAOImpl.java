package Projet.flow.dao;

import Projet.flow.model.ClasserDepense;
import Projet.flow.model.Transaction;
import Projet.flow.util.Session;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class TransactionDAOImpl extends AbstractDAO implements TransactionDAO {

    private static final String COLONNES = """
            id_transaction, nom, description, type, montant,
            date_transaction, id_compte, id_source""";

    public TransactionDAOImpl(Session session) {
        super(session);
    }

    private Transaction convertirEnTransaction(ResultSet rs) throws SQLException {
        Date dateSQL = rs.getDate("date_transaction");
        return new Transaction(
                rs.getInt("id_transaction"),
                rs.getString("nom"),
                rs.getString("description"),
                rs.getString("type"),
                rs.getBigDecimal("montant"),
                dateSQL != null ? dateSQL.toLocalDate() : null,
                rs.getInt("id_compte"),
                rs.getObject("id_source", Integer.class) // null pour une dépense
        );
    }

    private static Date versDate(LocalDate date) {
        return date != null ? Date.valueOf(date) : null;
    }

    /**
     * [{"id_categorie":1,"montant":61.00}, ...] pour JSON_TABLE dans la procédure.
     * Que des nombres : aucun risque d'injection.
     */
    private static String versJson(List<ClasserDepense> repartitions) {
        StringJoiner json = new StringJoiner(",", "[", "]");
        if (repartitions != null) {
            for (ClasserDepense v : repartitions) {
                json.add("{\"id_categorie\":" + v.getIdCategorie()
                        + ",\"montant\":" + v.getMontantAffecte().toPlainString() + "}");
            }
        }
        return json.toString();
    }

    @Override
    public int creerRevenu(Transaction revenu) throws SQLException {

        if (revenu.getIdSource() == null) {
            throw new IllegalArgumentException("Un revenu doit avoir une source");
        }

        String sql = "CALL enregistrer_revenu(?, ?, ?, ?, ?, ?, @id_transaction)";

        try (Connection conn = connexion()) {

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, revenu.getIdCompte());
                stmt.setString(2, revenu.getNom());
                stmt.setString(3, revenu.getDescription());
                stmt.setBigDecimal(4, revenu.getMontant());
                stmt.setDate(5, versDate(revenu.getDate()));
                stmt.setInt(6, revenu.getIdSource());
                stmt.execute();
            }

            int id = lireIdCree(conn);
            revenu.setIdTransaction(id);
            revenu.setType("REVENU");
            return id;

        } catch (SQLException e) {
            throw traduire(e, null, null);
        }
    }

    @Override
    public int creerDepense(Transaction depense, List<ClasserDepense> repartitions)
            throws SQLException {

        String sql = "CALL enregistrer_depense(?, ?, ?, ?, ?, ?, @id_transaction)";

        try (Connection conn = connexion()) {

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, depense.getIdCompte());
                stmt.setString(2, depense.getNom());
                stmt.setString(3, depense.getDescription());
                stmt.setBigDecimal(4, depense.getMontant());
                stmt.setDate(5, versDate(depense.getDate()));
                stmt.setString(6, versJson(repartitions));
                stmt.execute();
            }

            int id = lireIdCree(conn);
            depense.setIdTransaction(id);
            depense.setType("DEPENSE");
            depense.setIdSource(null);
            return id;

        } catch (SQLException e) {
            throw traduire(e, null, null);
        }
    }

    private static int lireIdCree(Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT @id_transaction");
             ResultSet rs = stmt.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    @Override
    public Transaction trouverParId(int idTransaction) throws SQLException {

        String sql = "SELECT " + COLONNES + " FROM v_mes_transactions WHERE id_transaction = ?";

        try (Connection conn = connexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idTransaction);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? convertirEnTransaction(rs) : null;
            }
        } catch (SQLException e) {
            throw traduire(e, null, null);
        }
    }

    @Override
    public List<Transaction> listerParCompte(int idCompte) throws SQLException {

        String sql = "SELECT " + COLONNES + """
                 FROM v_mes_transactions
                WHERE id_compte = ?
                ORDER BY date_transaction DESC, id_transaction DESC
                """;

        List<Transaction> transactions = new ArrayList<>();

        try (Connection conn = connexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idCompte);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(convertirEnTransaction(rs));
                }
            }
        } catch (SQLException e) {
            throw traduire(e, null, null);
        }
        return transactions;
    }

    @Override
    public List<Transaction> listerParPeriode(int idCompte, LocalDate dateDebut, LocalDate dateFin)
            throws SQLException {

        // Cette requête profite de l'index (id_compte, date_transaction)
        String sql = "SELECT " + COLONNES + """
                 FROM v_mes_transactions
                WHERE id_compte = ?
                  AND date_transaction BETWEEN ? AND ?
                ORDER BY date_transaction DESC, id_transaction DESC
                """;

        List<Transaction> transactions = new ArrayList<>();

        try (Connection conn = connexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idCompte);
            stmt.setDate(2, versDate(dateDebut));
            stmt.setDate(3, versDate(dateFin));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(convertirEnTransaction(rs));
                }
            }
        } catch (SQLException e) {
            throw traduire(e, null, null);
        }
        return transactions;
    }

    @Override
    public void modifier(Transaction transaction) throws SQLException {

        String sql = "{CALL modifier_transaction(?, ?, ?, ?, ?, ?)}";

        try (Connection conn = connexion();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setInt(1, transaction.getIdTransaction());
            stmt.setString(2, transaction.getNom());
            stmt.setString(3, transaction.getDescription());
            stmt.setBigDecimal(4, transaction.getMontant());
            stmt.setDate(5, versDate(transaction.getDate()));
            if (transaction.getIdSource() != null) {
                stmt.setInt(6, transaction.getIdSource());
            } else {
                stmt.setNull(6, Types.INTEGER);
            }

            stmt.execute();

        } catch (SQLException e) {
            throw traduire(e, null, null);
        }
    }

    @Override
    public void supprimer(int idTransaction) throws SQLException {

        String sql = "{CALL supprimer_transaction(?)}";

        try (Connection conn = connexion();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setInt(1, idTransaction);
            stmt.execute();

        } catch (SQLException e) {
            throw traduire(e, null, null);
        }
    }
}