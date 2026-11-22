package Projet.flow.dao;

import Projet.flow.model.Utilisateur;

import java.sql.SQLException;
import java.util.List;

public interface UtilisateurDAO {

    int creer(Utilisateur utilisateur) throws SQLException;

    Utilisateur trouverParId(int idUtilisateur) throws SQLException;

    Utilisateur trouverParEmail(String email) throws SQLException;

    List<Utilisateur> listerTous() throws SQLException;

    boolean modifier(Utilisateur utilisateur) throws SQLException;

    boolean changerMotDePasse(int idUtilisateur, String nouveauHash) throws SQLException;

    boolean supprimer(int idUtilisateur) throws SQLException;
}