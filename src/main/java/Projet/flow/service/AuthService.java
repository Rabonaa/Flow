package Projet.flow.service;

import Projet.flow.dao.AuthDAO;
import Projet.flow.dao.AuthDAOImpl;
import Projet.flow.model.Identifiants;
import Projet.flow.util.Session;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.sql.SQLException;
import java.util.Optional;

public class AuthService {

    private final AuthDAO authDAO = new AuthDAOImpl();
    private final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();

    public Optional<Session> connecter(String email, String motDePasse) throws SQLException {
        if (email == null || motDePasse == null) {
            return Optional.empty();
        }

        Optional<Identifiants> identifiants = authDAO.lireIdentifiants(email.trim());
        if (identifiants.isEmpty()) {
            return Optional.empty();
        }

        Identifiants i = identifiants.get();
        if (!bcrypt.matches(motDePasse, i.mdpHash())) {
            return Optional.empty();
        }

        return Optional.of(new Session(i.idUtilisateur(), i.role(), i.pseudo()));
    }

    public String hacher(String motDePasse) {
        if (motDePasse == null || motDePasse.length() < 8) {
            throw new IllegalArgumentException("Le mot de passe doit contenir au moins 8 caractères");
        }
        return bcrypt.encode(motDePasse);
    }
}