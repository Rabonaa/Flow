package Projet.flow.dao;

import Projet.flow.model.Compte;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class CompteDAOImpl implements CompteDAO{
    @Override
    public int creer(Compte compte) throws SQLException {

        return 0;
    }

    @Override
    public Compte trouverParId(int idCompte) throws SQLException {
        return null;
    }

    @Override
    public List<Compte> listerParUtilisateur(int idUtilisateur) throws SQLException {
        return List.of();
    }

    @Override
    public boolean modifier(Compte compte) throws SQLException {
        return false;
    }

    @Override
    public boolean supprimer(int idCompte) throws SQLException {
        return false;
    }

    @Override
    public BigDecimal calculerSolde(int idCompte) throws SQLException {
        return null;
    }
}
