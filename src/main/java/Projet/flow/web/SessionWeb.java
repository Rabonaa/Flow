package Projet.flow.web;

import Projet.flow.util.Session;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;

public final class SessionWeb {

    public static final String CLE = "session_flow";

    private SessionWeb() {}

    public static Session connecte(HttpSession http) {
        Object session = http.getAttribute(CLE);
        if (session == null) {
            throw new ErreurAcces(HttpStatus.UNAUTHORIZED, "Connexion requise");
        }
        return (Session) session;
    }

    public static Session utilisateur(HttpSession http) {
        Session session = connecte(http);
        if (session.estAdmin()) {
            throw new ErreurAcces(HttpStatus.FORBIDDEN, "Écran réservé aux utilisateurs");
        }
        return session;
    }

    public static Session admin(HttpSession http) {
        Session session = connecte(http);
        if (!session.estAdmin()) {
            throw new ErreurAcces(HttpStatus.FORBIDDEN, "Écran réservé à l'administrateur");
        }
        return session;
    }
}