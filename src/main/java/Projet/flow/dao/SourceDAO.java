package Projet.flow.dao;

import Projet.flow.model.Source;

import java.sql.SQLException;
import java.util.List;

/** Lecture pour tous, écriture réservée à l'ADMIN (imposé par les droits MySQL). */
public interface SourceDAO {

    int creer(Source source) throws SQLException;

    Source trouverParId(int idSource) throws SQLException;

    List<Source> listerToutes() throws SQLException;

    boolean modifier(Source source) throws SQLException;

    /** Refusé si la source est utilisée par un revenu. */
    boolean supprimer(int idSource) throws SQLException;
}