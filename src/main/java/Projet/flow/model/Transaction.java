package Projet.flow.model;
import java.math.BigDecimal;
import java.time.LocalDate;

public class Transaction {
    private int idTransaction;
    private String nom;
    private String description;
    private String type;
    private BigDecimal montant;
    private LocalDate date;
    private int idCompte;
    private Integer idSource;

    //Constructeurs
    public Transaction(){};
    public Transaction(int idTransaction, String nom, String description, String type,
                       BigDecimal montant, LocalDate date, int idCompte, Integer idSource){
        this.idTransaction = idTransaction;
        this.nom = nom;
        this.description = description;
        this.type = type;
        this.montant = montant;
        this.date = date;
        this.idCompte = idCompte;
        this.idSource = idSource;
    }
    public Transaction(String nom, String description, String type,
                       BigDecimal montant, LocalDate date, int idCompte, Integer idSource){
        this.nom = nom;
        this.description = description;
        this.type = type;
        this.montant = montant;
        this.date = date;
        this.idCompte = idCompte;
        this.idSource = idSource;
    }
    public Transaction(String nom, String description, String type,
                       BigDecimal montant, LocalDate date, int idCompte){
        this.nom = nom;
        this.description = description;
        this.type = type;
        this.montant = montant;
        this.date = date;
        this.idCompte = idCompte;
        this.idSource = null;
    }

    //Getters
    public int getIdTransaction(){return idTransaction;}
    public String getNom(){return nom;}
    public String getDescription(){return description;}
    public String getType(){return type;}
    public BigDecimal getMontant(){return montant;}
    public LocalDate getDate(){return date;}
    public int getIdCompte(){return idCompte;}
    public Integer getIdSource(){return idSource;}

    //Setters
    public void setIdTransaction(int idTransaction){this.idTransaction = idTransaction;}
    public void setNom(String nom){this.nom = nom;}
    public void setDescription(String description) {this.description = description;}
    public void setType(String type){this.type = type;}
    public void setMontant(BigDecimal montant){this.montant = montant;}
    public void setDate(LocalDate date){this.date = date;}
    public void setIdCompte(int idCompte){this.idCompte = idCompte;}
    public void setIdSource(Integer idSource){this.idSource = idSource;}

    //toString
    public String toString(){
        return "Transaction " + idTransaction + "\n"
                + "nom : " + nom + "\n"
                + "description : " + description + "\n"
                + "type : " + type + "\n"
                + "montant : " + montant + "\n"
                + "date : " + date + "\n"
                + "idCompte : " + idCompte + "\n"
                + "source : " + idSource + "\n";
    }
}