package Projet.flow.ui;

import Projet.flow.util.ConnectionManager;

import java.sql.Connection;
import java.sql.SQLException;

public class TestConnection {
    public static void main(String[] args){
        System.out.println("Hello world");
        System.out.println("Tentative de connexion à la bdd");
        try (Connection conn = ConnectionManager.getConnection()){
            System.out.println("Connexion réussie.");
            System.out.println("Catalogue : " + conn.getCatalog());
            System.out.println("Auto-commit actuel : " + conn.getAutoCommit());
        } catch(UnsupportedOperationException e){
            System.out.println("Connexion pas implémentée" + e.getMessage());
        } catch (SQLException e){
            System.err.println("Connexion échouée : " + e.getMessage());
            System.err.println("Vérifiez l'URL, nom d'utilisateur et mdp");
        }
    }
}
