package Projet.flow.dao;

import Projet.flow.model.Devise;
import java.sql.SQLException;
import java.util.List;

public interface DeviseDAO {

    int creer(Devise devise) throws SQLException;

    Devise trouverParId(int idDevise) throws SQLException;

    Devise trouverParCode(String code) throws SQLException;

    List<Devise> listerToutes() throws SQLException;

    boolean modifier(Devise devise) throws SQLException;

    boolean supprimer(int idDevise) throws SQLException;
}