package Projet.flow.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record LigneTableauBord(
        LocalDate mois,
        String devise,
        String type,
        String rubrique,
        BigDecimal total,
        int nbOperations,
        LocalDateTime rafraichiLe
) {}