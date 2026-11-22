package Projet.flow.model;
import java.math.BigDecimal;

public class Compte {
    private int idCompte;
    private String nom;
    private String type;
    private BigDecimal montantInitial;
    private int idDevise;
    private int idUtilisateur;


    //Constructeurs
    public Compte(){};
    public Compte(int idCompte, String nom, String type, BigDecimal montantInitial, int idDevise, int idUtilisateur){
        this.idCompte = idCompte;
        this.nom = nom;
        this.type = type;
        this.montantInitial = montantInitial;
        this.idDevise = idDevise;
        this.idUtilisateur = idUtilisateur;
    }
    public Compte( String nom, String type, BigDecimal montantInitial, int idDevise, int idUtilisateur){
        this.nom = nom;
        this.type = type;
        this.montantInitial = montantInitial;
        this.idDevise = idDevise;
        this.idUtilisateur = idUtilisateur;
    }

    //Getters
    public int getIdCompte(){return idCompte;}
    public String getNom(){return nom;}
    public String getType(){return type;}
    public BigDecimal getMontantInitial(){return montantInitial;}
    public int getIdDevise(){return idDevise;}
    public int getIdUtilisateur(){return idUtilisateur;}

    //Setters
    public void setIdCompte(int idCompte){this.idCompte = idCompte;}
    public void setNom(String nom){this.nom = nom;}
    public void setType(String type){this.type = type;}
    public void setMontantInitial(BigDecimal montantInitial){this.montantInitial = montantInitial;}
    public void setIdDevise(int idDevise){this.idDevise = idDevise;}
    public void setIdUtilisateur(int idUtilisateur){this.idUtilisateur = idUtilisateur;}

    //toString()
    @Override
    public String toString(){
        return "Compte " + idCompte + ": \n"
                + nom + "\n"
                + type + "\n"
                + "Solde : " + montantInitial +"\n"
                + "Devise : " + idDevise + "\n"
                + "idUtilisateur : " + idUtilisateur + "\n";
    }

}
