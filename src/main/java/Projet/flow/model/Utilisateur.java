package Projet.flow.model;

import java.time.LocalDate;

public class Utilisateur {
    private int idUtilisateur;
    private String role;
    private String pseudo;
    private String email;
    private String mdp;
    private LocalDate dateInscription;

    //Constructeurs
    public Utilisateur(){};
    public Utilisateur(int idUtilisateur, String role, String pseudo, String email,
                       String mdp, LocalDate dateInscription){
        this.idUtilisateur = idUtilisateur;
        this.role = role;
        this.pseudo = pseudo;
        this.email = email;
        this.mdp = mdp;
        this.dateInscription = dateInscription;
    }
    public Utilisateur(String role, String pseudo, String email,
                       String mdp, LocalDate dateInscription){
        this.role = role;
        this.pseudo = pseudo;
        this.email = email;
        this.mdp = mdp;
        this.dateInscription = dateInscription;
    }
    //Getters
    public int getIdUtilisateur(){return idUtilisateur;}
    public String getRole(){return role;}
    public String getPseudo(){return pseudo;}
    public String getEmail(){return email;}
    public String getMdp(){return mdp;}
    public LocalDate getDateInscription(){return dateInscription;}

    //Setters
    public void setIdUtilisateur(int idUtilisateur){this.idUtilisateur = idUtilisateur;}
    public void setRole(String role){this.role = role;}
    public void setPseudo(String pseudo){this.pseudo = pseudo;}
    public void setEmail(String email){this.email = email;}
    public void setMdp(String mdp){this.mdp = mdp;}
    public void setDateInscription(LocalDate dateInscription){this.dateInscription = dateInscription;}

    //toString
    public String toString(){
        return "Utilisateur : "+ idUtilisateur + "\n"
                + "role : " + role + "\n"
                + "pseudo : " + pseudo + "\n"
                + "email : " + email + "\n"
                + "mdp : ...." + "\n"

                + "date d inscription : " + dateInscription + "\n";
    }
}