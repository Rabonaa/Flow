package Projet.flow.dao;

import Projet.flow.model.Categorie;
import java.sql.SQLException;
import java.util.List;

public interface CategorieDAO {

    int creer(Categorie categorie) throws SQLException;

    Categorie trouverParId(int idCategorie) throws SQLException;

    List<Categorie> listerToutes() throws SQLException;

    boolean modifier(Categorie categorie) throws SQLException;

    boolean supprimer(int idCategorie) throws SQLException;
}
