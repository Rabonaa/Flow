package Projet.flow.dao;

import Projet.flow.model.Classer_depense;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public interface ClasserDepenseDAO {

    boolean affecter(Classer_depense affectation)
            throws SQLException;

    boolean modifierAffectation(Classer_depense affectation)
            throws SQLException;

    boolean supprimerAffectation(
            int idTransaction,
            int idCategorie
    ) throws SQLException;

    List<Classer_depense> listerParTransaction(
            int idTransaction
    ) throws SQLException;

    BigDecimal calculerTotalAffecte(int idTransaction)
            throws SQLException;
}