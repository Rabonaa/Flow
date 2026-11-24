package Projet.flow.dao;

import Projet.flow.model.ClasserDepense;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

/**
 * Repartition d'une dépense existante sur les catégories.
 * Règles (vérifiées par trigger) : dépense uniquement, total <= montant.
 */
public interface ClasserDepenseDAO {

    void affecter(ClasserDepense affectation) throws SQLException;

    void modifierAffectation(ClasserDepense affectation) throws SQLException;

    void supprimerAffectation(int idTransaction, int idCategorie) throws SQLException;

    List<ClasserDepense> listerParTransaction(int idTransaction) throws SQLException;

    BigDecimal calculerTotalAffecte(int idTransaction) throws SQLException;
}