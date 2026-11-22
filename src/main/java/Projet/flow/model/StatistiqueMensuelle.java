package Projet.flow.model;

public record StatistiqueMensuelle(
        String mois,
        int utilisateursActifs,
        int nbTransactions,
        int nbRevenus,
        int nbDepenses
) {}