package Projet.flow.model;

public class Categorie {
    private int idCategorie;
    private String nom;

    //Constructeurs
    public Categorie(){};
    public Categorie(int idCategorie, String nom){
        this.idCategorie = idCategorie;
        this.nom = nom;
    }

    //Getters
    public int getIdCategorie(){return idCategorie;}
    public String getNom(){return nom;}

    //Setters
    public void setIdCategorie(int idCategorie){this.idCategorie = idCategorie;}
    public void setNom(String nom){this.nom = nom;}

    //toString()
    @Override
    public String toString(){
        return "Categorie " + idCategorie + " : "
                + nom + "\n";
    }

}
