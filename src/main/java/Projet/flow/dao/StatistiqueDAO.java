package Projet.flow.dao;

import Projet.flow.model.StatistiqueMensuelle;

import java.sql.SQLException;
import java.util.List;

public interface StatistiqueDAO {

    List<StatistiqueMensuelle> statistiquesMensuelles() throws SQLException;

    void rafraichirTableauBord() throws SQLException;
}