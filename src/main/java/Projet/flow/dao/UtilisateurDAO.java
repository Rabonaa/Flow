package Projet.flow.dao;
import Projet.flow.model.Utilisateur;
import java.sql.SQLException;

public interface UtilisateurDAO {

    int creer(Utilisateur utilisateur) throws SQLException;

    Utilisateur trouverParId(int idUtilisateur) throws SQLException;

    Utilisateur trouverParEmail(String email) throws SQLException;

    boolean modifier(Utilisateur utilisateur) throws SQLException;

    boolean supprimer(int idUtilisateur) throws SQLException;
}