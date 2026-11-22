package Projet.flow.dao;
import Projet.flow.model.Compte;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public interface CompteDAO {

    int creer(Compte compte) throws SQLException;

    Compte trouverParId(int idCompte) throws SQLException;

    List<Compte> listerParUtilisateur(int idUtilisateur)
            throws SQLException;

    boolean modifier(Compte compte) throws SQLException;

    boolean supprimer(int idCompte) throws SQLException;

    BigDecimal calculerSolde(int idCompte) throws SQLException;
}