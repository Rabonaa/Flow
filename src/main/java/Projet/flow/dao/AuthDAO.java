package Projet.flow.dao;

import Projet.flow.model.Identifiants;

import java.sql.SQLException;
import java.util.Optional;

public interface AuthDAO {

    /** Vide si l'e-mail est inconnu. La vérification BCrypt se fait ensuite côté Java. */
    Optional<Identifiants> lireIdentifiants(String email) throws SQLException;
}