package Projet.flow.web;

import Projet.flow.dao.ClasserDepenseDAOImpl;
import Projet.flow.dao.CompteDAOImpl;
import Projet.flow.dao.TransactionDAO;
import Projet.flow.dao.TransactionDAOImpl;
import Projet.flow.model.ClasserDepense;
import Projet.flow.model.Transaction;
import Projet.flow.util.Session;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class TransactionController {

    public record RevenuDemande(int idCompte, String nom, String description,
                                BigDecimal montant, LocalDate date, Integer idSource) {}

    public record LigneRepartition(int idCategorie, BigDecimal montant) {}

    public record DepenseDemande(int idCompte, String nom, String description,
                                 BigDecimal montant, LocalDate date, List<LigneRepartition> repartitions) {}

    @GetMapping("/comptes/{id}/transactions")
    public List<Transaction> transactionsDuCompte(@PathVariable("id") int id, HttpSession http) throws SQLException {
        return new TransactionDAOImpl(SessionWeb.utilisateur(http)).listerParCompte(id);
    }

    @GetMapping("/transactions/{id}/repartitions")
    public List<ClasserDepense> repartitions(@PathVariable("id") int id, HttpSession http) throws SQLException {
        return new ClasserDepenseDAOImpl(SessionWeb.utilisateur(http)).listerParTransaction(id);
    }

    @PostMapping("/revenus")
    public Map<String, Object> revenu(@RequestBody RevenuDemande d, HttpSession http) throws SQLException {
        Session session = SessionWeb.utilisateur(http);
        Transaction revenu = new Transaction(d.nom(), d.description(), "REVENU", d.montant(),
                d.date() != null ? d.date() : LocalDate.now(), d.idCompte(), d.idSource());
        int id = new TransactionDAOImpl(session).creerRevenu(revenu);
        return Map.of("idTransaction", id,
                "solde", new CompteDAOImpl(session).calculerSolde(d.idCompte()));
    }

    @PostMapping("/depenses")
    public Map<String, Object> depense(@RequestBody DepenseDemande d, HttpSession http) throws SQLException {
        Session session = SessionWeb.utilisateur(http);
        Transaction depense = new Transaction(d.nom(), d.description(), "DEPENSE", d.montant(),
                d.date() != null ? d.date() : LocalDate.now(), d.idCompte(), null);

        List<ClasserDepense> repartitions = new ArrayList<>();
        if (d.repartitions() != null) {
            for (LigneRepartition v : d.repartitions()) {
                repartitions.add(new ClasserDepense(0, v.idCategorie(), v.montant()));
            }
        }

        int id = new TransactionDAOImpl(session).creerDepense(depense, repartitions);
        return Map.of("idTransaction", id,
                "solde", new CompteDAOImpl(session).calculerSolde(d.idCompte()));
    }

    @DeleteMapping("/transactions/{id}")
    public Map<String, Boolean> supprimer(@PathVariable("id") int id, HttpSession http) throws SQLException {
        TransactionDAO dao = new TransactionDAOImpl(SessionWeb.utilisateur(http));
        dao.supprimer(id);
        return Map.of("supprime", true);
    }
}