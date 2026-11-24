package Projet.flow.web;

import Projet.flow.dao.TableauBordDAO;
import Projet.flow.dao.TableauBordDAOImpl;
import Projet.flow.model.DepenseCategorie;
import Projet.flow.model.LigneTableauBord;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/tableau-de-bord")
public class TableauBordController {

    public record TableauBord(List<LigneTableauBord> synthese, List<DepenseCategorie> categories) {}

    @GetMapping
    public TableauBord tableauBord(@RequestParam(value = "mois", defaultValue = "3") int mois, HttpSession http) throws SQLException {
        TableauBordDAO dao = new TableauBordDAOImpl(SessionWeb.utilisateur(http));
        LocalDate depuis = LocalDate.now().withDayOfMonth(1).minusMonths(mois - 1L);
        return new TableauBord(dao.lireTableauBord(depuis), dao.depensesParCategorie(mois));
    }
}