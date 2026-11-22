package Projet.flow.model;
import java.math.BigDecimal;

public class ClasserDepense {
    private int idTransaction;
    private int idCategorie;
    private BigDecimal montantAffecte;

    public ClasserDepense(){};
    public ClasserDepense(int idTransaction, int idCategorie, BigDecimal montantAffecte){
        this.idTransaction = idTransaction;
        this.idCategorie = idCategorie;
        this.montantAffecte = montantAffecte;
    }

    //Getters
    public int getIdTransaction(){return idTransaction;}
    public int getIdCategorie(){return idCategorie;}
    public BigDecimal getMontantAffecte(){return montantAffecte;}

    //Setters
    public void setIdTransaction(int idTransaction){this.idTransaction = idTransaction;}
    public void setIdCategorie(int idCategorie){this.idCategorie = idCategorie;}
    public void setMontantAffecte(BigDecimal montantAffecte) {this.montantAffecte = montantAffecte;}

    //toString()
    @Override
    public String toString(){
        return "ClasserDepense : \n"
                + "Transaction : " + idTransaction + "\n"
                + "Categorie : " + idCategorie + "\n";
    }

}
