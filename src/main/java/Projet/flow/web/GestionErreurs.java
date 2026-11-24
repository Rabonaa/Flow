package Projet.flow.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLException;
import java.util.Map;

@RestControllerAdvice
public class GestionErreurs {

    @ExceptionHandler(ErreurAcces.class)
    public ResponseEntity<Map<String, String>> acces(ErreurAcces e) {
        return ResponseEntity.status(e.getStatut()).body(Map.of("erreur", e.getMessage()));
    }

    @ExceptionHandler(SQLException.class)
    public ResponseEntity<Map<String, String>> baseDeDonnees(SQLException e) {
        HttpStatus statut;
        String message = e.getMessage();

        switch (e.getErrorCode()) {
            case 1644 -> statut = message != null && message.contains("introuvable") ? HttpStatus.NOT_FOUND
                    : HttpStatus.UNPROCESSABLE_ENTITY;
            case 1142, 1143, 1370, 1369 -> statut = HttpStatus.FORBIDDEN;
            case 1062, 1216, 1217, 1451, 1452, 3819 -> statut = HttpStatus.CONFLICT;
            default -> {
                statut = HttpStatus.INTERNAL_SERVER_ERROR;
                message = "Erreur de la base de données : " + message;
            }
        }
        return ResponseEntity.status(statut).body(Map.of("erreur", message));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> requeteInvalide(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("erreur", e.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> interdit(IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("erreur", e.getMessage()));
    }
}