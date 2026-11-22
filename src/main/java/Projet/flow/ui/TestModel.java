package Projet.flow.ui;

import Projet.flow.model.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public class TestModel {

    public static void main(String[] args) {

        Utilisateur utilisateur1 = new Utilisateur(
                "USER", "Alex", "alex@gmail.com",
                "password", LocalDate.of(2026, 11, 22)
        );
        System.out.println(utilisateur1);

        Devise devise1 = new Devise("EUR");
        System.out.println(devise1);

        Categorie categorie1 = new Categorie("Alimentation");
        System.out.println(categorie1);

        Source source1 = new Source("Salaire");
        System.out.println(source1);

        Compte compte1 = new Compte(
                "Compte courant", "COURANT",
                new BigDecimal("1500.00"), 1, 1
        );
        System.out.println(compte1);

        Transaction transaction1 = new Transaction(
                "Courses", "Courses alimentaires",
                "DEPENSE", new BigDecimal("45.90"),
                LocalDate.of(2026, 11, 22), 1, null
        );
        System.out.println(transaction1);

        ClasserDepense affectation1 = new ClasserDepense(
                1, 1, new BigDecimal("45.90")
        );
        System.out.println(affectation1);

    }
}