package Projet.flow.model;

public class Devise {
    private int idDevise;
    private String code;

    //Constructeurs
    public Devise(){}
    public Devise(int idDevise, String code){
        this.idDevise = idDevise;
        this.code = code;
    }
    public Devise(String code){
        this.code = code;
    }

    //Getters
    public int getIdDevise(){return this.idDevise;}
    public String getCode(){return this.code;}

    //Setters
    public void setIdDevise(int idDevise){this.idDevise = idDevise;}
    public void setCode(String code){this.code = code;}

    //toString
    public String toString(){
        return "Devise : " + idDevise + ": " + code + "\n";
    }
}
