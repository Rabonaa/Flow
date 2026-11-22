package Projet.flow.dao;

import Projet.flow.util.ConnectionManager;
import Projet.flow.util.Session;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

/** Base commune : session, ouverture de connexion et messages d'erreur lisibles. */
abstract class AbstractDAO {

    protected final Session session;

    protected AbstractDAO(Session session) {
        this.session = Objects.requireNonNull(session, "Session requise");
    }

    protected Connection connexion() throws SQLException {
        return ConnectionManager.getConnection(session);
    }

    /**
     * Traduit les codes d'erreur MySQL techniques en messages affichables.
     * Les erreurs métier (SIGNAL '45000' des triggers et procédures)
     * gardent leur message d'origine.
     */
    protected static SQLException traduire(SQLException e, String siDoublon, String siUtilise) {
        String message = switch (e.getErrorCode()) {
            case 1062 -> siDoublon;                                   // UNIQUE violé
            case 1451, 1217 -> siUtilise;                                   // FK : ligne encore référencée
            case 1452 -> "Référence inexistante";                     // FK : parent introuvable
            case 1142, 1143, 1370 -> "Action non autorisée pour ce rôle"; // droit refusé
            case 3819 -> "Valeur refusée par une contrainte";         // CHECK violé
            default -> null;
        };
        return message == null ? e : new SQLException(message, e.getSQLState(), e.getErrorCode(), e);
    }
}