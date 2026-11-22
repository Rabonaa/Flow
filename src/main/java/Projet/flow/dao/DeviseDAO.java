package Projet.flow.dao;

import Projet.flow.model.Devise;

import java.sql.SQLException;
import java.util.List;

/** Lecture pour tous, écriture réservée à l'ADMIN (imposé par les droits MySQL). */
public interface DeviseDAO {

    int creer(Devise devise) throws SQLException;

    Devise trouverParId(int idDevise) throws SQLException;

    Devise trouverParCode(String code) throws SQLException;

    List<Devise> listerToutes() throws SQLException;

    boolean modifier(Devise devise) throws SQLException;

    /** Refusé si un compte utilise cette devise. */
    boolean supprimer(int idDevise) throws SQLException;
}