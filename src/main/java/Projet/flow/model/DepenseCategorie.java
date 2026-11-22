package Projet.flow.model;

import java.math.BigDecimal;

public record DepenseCategorie(
        String categorie,
        int nbDepenses,
        BigDecimal total,
        BigDecimal partPourcent
) {}