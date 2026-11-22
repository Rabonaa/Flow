package Projet.flow.model;

public record Identifiants(int idUtilisateur, String role, String pseudo, String mdpHash) {

    @Override
    public String toString() {
        return "Identifiants[" + idUtilisateur + ", " + role + ", " + pseudo + "]";
    }
}