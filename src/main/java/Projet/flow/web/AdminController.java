package Projet.flow.web;

import Projet.flow.dao.CategorieDAOImpl;
import Projet.flow.dao.StatistiqueDAOImpl;
import Projet.flow.dao.UtilisateurDAOImpl;
import Projet.flow.model.Categorie;
import Projet.flow.model.StatistiqueMensuelle;
import Projet.flow.model.Utilisateur;
import Projet.flow.service.AuthService;
import Projet.flow.util.Session;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    public record UtilisateurDemande(String role, String pseudo, String email, String motDePasse) {}

    public record CategorieDemande(String nom) {}

    private final AuthService authService = new AuthService();

    @GetMapping("/utilisateurs")
    public List<Utilisateur> utilisateurs(HttpSession http) throws SQLException {
        return new UtilisateurDAOImpl(SessionWeb.admin(http)).listerTous();
    }

    @PostMapping("/utilisateurs")
    public Utilisateur creerUtilisateur(@RequestBody UtilisateurDemande d, HttpSession http) throws SQLException {
        Utilisateur utilisateur = new Utilisateur(d.role() != null ? d.role() : "USER", d.pseudo(), d.email(),
                authService.hacher(d.motDePasse()), null);
        new UtilisateurDAOImpl(SessionWeb.admin(http)).creer(utilisateur);
        return utilisateur;
    }

    @DeleteMapping("/utilisateurs/{id}")
    public Map<String, Boolean> supprimerUtilisateur(@PathVariable("id") int id, HttpSession http) throws SQLException {
        Session admin = SessionWeb.admin(http);
        if (id == admin.idUtilisateur()) {
            throw new IllegalArgumentException("Un administrateur ne peut pas supprimer son propre compte");
        }
        return Map.of("supprime", new UtilisateurDAOImpl(admin).supprimer(id));
    }

    @PostMapping("/categories")
    public Categorie creerCategorie(@RequestBody CategorieDemande d, HttpSession http) throws SQLException {
        Categorie categorie = new Categorie(d.nom());
        new CategorieDAOImpl(SessionWeb.admin(http)).creer(categorie);
        return categorie;
    }

    @GetMapping("/statistiques")
    public List<StatistiqueMensuelle> statistiques(HttpSession http) throws SQLException {
        return new StatistiqueDAOImpl(SessionWeb.admin(http)).statistiquesMensuelles();
    }

    @PostMapping("/rafraichir")
    public Map<String, Boolean> rafraichir(HttpSession http) throws SQLException {
        new StatistiqueDAOImpl(SessionWeb.admin(http)).rafraichirTableauBord();
        return Map.of("rafraichi", true);
    }
}