package Projet.flow.dao;

import Projet.flow.model.DepenseCategorie;
import Projet.flow.model.LigneTableauBord;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public interface TableauBordDAO {

    List<LigneTableauBord> lireTableauBord(LocalDate depuis) throws SQLException;

    List<DepenseCategorie> depensesParCategorie(int nbMois) throws SQLException;
}