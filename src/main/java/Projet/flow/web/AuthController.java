package Projet.flow.web;

import Projet.flow.service.AuthService;
import Projet.flow.util.Session;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AuthController {

    public record ConnexionDemande(String email, String motDePasse) {}

    private final AuthService authService = new AuthService();

    @PostMapping("/connexion")
    public Session connexion(@RequestBody ConnexionDemande demande, HttpSession http) throws SQLException {
        Session session = authService.connecter(demande.email(), demande.motDePasse())
                .orElseThrow(() -> new ErreurAcces(HttpStatus.UNAUTHORIZED, "E-mail ou mot de passe incorrect"));
        http.setAttribute(SessionWeb.CLE, session);
        return session;
    }

    @PostMapping("/deconnexion")
    public Map<String, Boolean> deconnexion(HttpSession http) {
        http.invalidate();
        return Map.of("deconnecte", true);
    }

    @GetMapping("/session")
    public Session session(HttpSession http) {
        return SessionWeb.connecte(http);
    }
}