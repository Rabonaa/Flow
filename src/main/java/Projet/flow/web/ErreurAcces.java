package Projet.flow.web;

import org.springframework.http.HttpStatus;

public class ErreurAcces extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final HttpStatus statut;

    public ErreurAcces(HttpStatus statut, String message) {
        super(message);
        this.statut = statut;
    }

    public HttpStatus getStatut() {
        return statut;
    }
}