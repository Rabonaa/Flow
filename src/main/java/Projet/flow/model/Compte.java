package Projet.flow.model;
import java.math.BigDecimal;

public class Compte {
    private int idCompte;
    private String nom;
    private String type;
    private BigDecimal montantInitial;

    //Constructeur
    public Compte(){};
    public Compte(int idCompte, String nom, String type, BigDecimal montantInitial){
        this.idCompte = idCompte;
        this.nom = nom;
        this.type = type;
        this.montantInitial = montantInitial;
    }

    //Getters
    public int getIdCompte(){return idCompte;}
    public String getNom(){return nom;}
    public String getType(){return type;}
    public BigDecimal getMontantInitial(){return montantInitial;}

    //Setters
    public void setIdCompte(int idCompte){this.idCompte = idCompte;}
    public void setNom(String nom){this.nom = nom;}
    public void setType(String type){this.type = type;}
    public void setMontantInitial(BigDecimal montantInitial){this.montantInitial = montantInitial;}

    //toString()
    @Override
    public String toString(){
        return "Compte " + idCompte + ": \n"
                + nom + "\n"
                + type + "\n"
                + "Solde : " + montantInitial;
    }

}
