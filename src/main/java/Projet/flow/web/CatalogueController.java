package Projet.flow.web;

import Projet.flow.dao.CategorieDAOImpl;
import Projet.flow.dao.DeviseDAOImpl;
import Projet.flow.dao.SourceDAOImpl;
import Projet.flow.model.Categorie;
import Projet.flow.model.Devise;
import Projet.flow.model.Source;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("/api")
public class CatalogueController {

    @GetMapping("/categories")
    public List<Categorie> categories(HttpSession http) throws SQLException {
        return new CategorieDAOImpl(SessionWeb.connecte(http)).listerToutes();
    }

    @GetMapping("/sources")
    public List<Source> sources(HttpSession http) throws SQLException {
        return new SourceDAOImpl(SessionWeb.connecte(http)).listerToutes();
    }

    @GetMapping("/devises")
    public List<Devise> devises(HttpSession http) throws SQLException {
        return new DeviseDAOImpl(SessionWeb.connecte(http)).listerToutes();
    }
}