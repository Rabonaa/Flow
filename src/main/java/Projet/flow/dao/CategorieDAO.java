package Projet.flow.dao;

import Projet.flow.model.Categorie;

import java.sql.SQLException;
import java.util.List;

/** Lecture pour tous, écriture réservée à l'ADMIN (imposé par les droits MySQL). */
public interface CategorieDAO {

    int creer(Categorie categorie) throws SQLException;

    Categorie trouverParId(int idCategorie) throws SQLException;

    List<Categorie> listerToutes() throws SQLException;

    boolean modifier(Categorie categorie) throws SQLException;

    /** Refusé si la catégorie est utilisée dans une ventilation. */
    boolean supprimer(int idCategorie) throws SQLException;
}