package Projet.flow.dao;

import Projet.flow.model.ClasserDepense;
import Projet.flow.model.Transaction;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Lectures via la vue v_mes_transactions, écritures via procédures stockées
 * (atomicité, verrou sur le compte, règles métier vérifiées par la base).
 */
public interface TransactionDAO {

    /** La source est obligatoire. */
    int creerRevenu(Transaction revenu) throws SQLException;

    /** Ventilation facultative (liste vide = dépense non classée). Tout ou rien. */
    int creerDepense(Transaction depense, List<ClasserDepense> ventilations) throws SQLException;

    /** null si absente ou pas à l'utilisateur connecté. */
    Transaction trouverParId(int idTransaction) throws SQLException;

    List<Transaction> listerParCompte(int idCompte) throws SQLException;

    /** Bornes incluses. */
    List<Transaction> listerParPeriode(int idCompte, LocalDate dateDebut, LocalDate dateFin)
            throws SQLException;

    /** Compte et type non modifiables. Lève une erreur si la transaction est introuvable. */
    void modifier(Transaction transaction) throws SQLException;

    /** Supprime aussi ses ventilations. Lève une erreur si la transaction est introuvable. */
    void supprimer(int idTransaction) throws SQLException;
}