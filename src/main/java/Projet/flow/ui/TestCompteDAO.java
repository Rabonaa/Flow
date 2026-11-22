package Projet.flow.ui;

import Projet.flow.dao.CompteDAO;
import Projet.flow.dao.CompteDAOImpl;

public class TestCompteDAO {

    public static void main(String[] args) throws Exception {

        CompteDAO dao = new CompteDAOImpl();

        System.out.println("Solde du compte 1 : "
                + dao.calculerSolde(1));
    }
}