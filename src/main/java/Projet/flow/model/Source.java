package Projet.flow.model;

public class Source {
    private int idSource;
    private String nom;

    public Source(){};
    public Source(int idSource, String nom){
        this.idSource = idSource;
        this.nom = nom;
    }
    public Source(String nom){
        this.nom = nom;
    }

    //Getters
    public int getIdSource(){return idSource;}
    public String getNom(){return nom;}

    //Setters
    public void setIdSource(int idSource){this.idSource = idSource;}
    public void setNom(String nom){this.nom = nom;}

    //toString
    public String toString(){
        return "Source " + idSource + "\n"
                + "nom : " + nom + "\n";
    }
}
