package Projet.flow.dao;

import Projet.flow.model.Source;
import java.sql.SQLException;
import java.util.List;

public interface SourceDAO {

    int creer(Source source) throws SQLException;

    Source trouverParId(int idSource) throws SQLException;

    List<Source> listerToutes() throws SQLException;

    boolean modifier(Source source) throws SQLException;

    boolean supprimer(int idSource) throws SQLException;
}