package Projet.flow.web;

import Projet.flow.dao.CompteDAO;
import Projet.flow.dao.CompteDAOImpl;
import Projet.flow.model.Compte;
import Projet.flow.util.Session;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/comptes")
public class CompteController {

    public record CompteDemande(String nom, String type, BigDecimal montantInitial, int idDevise) {}

    public record CompteAvecSolde(Compte compte, BigDecimal solde) {}

    @GetMapping
    public List<CompteAvecSolde> mesComptes(HttpSession http) throws SQLException {
        CompteDAO dao = new CompteDAOImpl(SessionWeb.utilisateur(http));
        List<CompteAvecSolde> resultat = new ArrayList<>();
        for (Compte compte : dao.listerMesComptes()) {
            resultat.add(new CompteAvecSolde(compte, dao.calculerSolde(compte.getIdCompte())));
        }
        return resultat;
    }

    @PostMapping
    public Compte creer(@RequestBody CompteDemande demande, HttpSession http) throws SQLException {
        Session session = SessionWeb.utilisateur(http);
        Compte compte = new Compte(demande.nom(), demande.type(),
                demande.montantInitial() != null ? demande.montantInitial() : BigDecimal.ZERO,
                demande.idDevise(), session.idUtilisateur());
        new CompteDAOImpl(session).creer(compte);
        return compte;
    }

    @DeleteMapping("/{id}")
    public Map<String, Boolean> supprimer(@PathVariable("id") int id, HttpSession http) throws SQLException {
        return Map.of("supprime", new CompteDAOImpl(SessionWeb.utilisateur(http)).supprimer(id));
    }
}