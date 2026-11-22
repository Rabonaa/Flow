package Projet.flow.dao;
import Projet.flow.model.Transaction;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public interface TransactionDAO {

    int creer(Transaction transaction) throws SQLException;

    Transaction trouverParId(int idTransaction)
            throws SQLException;

    List<Transaction> listerParCompte(int idCompte)
            throws SQLException;

    List<Transaction> listerParPeriode(
            int idCompte,
            LocalDate dateDebut,
            LocalDate dateFin
    ) throws SQLException;

    boolean modifier(Transaction transaction) throws SQLException;

    boolean supprimer(int idTransaction) throws SQLException;
}