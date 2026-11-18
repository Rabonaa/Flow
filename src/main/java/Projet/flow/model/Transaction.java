package Projet.flow.model;

import java.time.LocalDate;

public class Transaction {
    private int idTransaction;
    private String nom;
    private String description;
    private String type;
    private float montant;
    private LocalDate date;

    //Constructeurs
    public Transaction(){};
    public Transaction(int idTransaction, String nom, String description,
                       String type, float montant, LocalDate date){
        this.idTransaction = idTransaction;
        this.nom = nom;
        this.description = description;
        this.type = type;
        this.montant = montant;
        this.date = date;
    }

    //Getters
    public int getIdTransaction(){return idTransaction;}
    public String getNom(){return nom;}
    public String getDescription(){return description;}
    public String getType(){return type;}
    public float getMontant(){return montant;}
    public LocalDate getDate(){return date;}

    //Setters
    public void setIdTransaction(int idTransaction){this.idTransaction = idTransaction;}
    public void setNom(String nom){this.nom = nom;}
    public void setType(String type){this.type = type;}
    public void setMontant(float montant){this.montant = montant;}
    public void setDate(LocalDate date){this.date = date;}

    //toString
    public String toString(){
        return "Transaction " + idTransaction + "\n"
                + "nom : " + nom + "\n"
                + "description : " + description + "\n"
                + "type : " + type + "\n"
                + "montant : " + montant + "\n"
                + "date : " + date + "\n";
    }
}