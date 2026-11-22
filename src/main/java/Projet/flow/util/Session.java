package Projet.flow.util;

/**
 * Utilisateur connecté. Créée après une authentification réussie,
 * puis transmise à chaque DAO.
 */
public record Session(int idUtilisateur, String role, String pseudo) {

    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_USER = "USER";

    public Session {
        if (!ROLE_ADMIN.equals(role) && !ROLE_USER.equals(role)) {
            throw new IllegalArgumentException("Rôle inconnu : " + role);
        }
    }

    public boolean estAdmin() {
        return ROLE_ADMIN.equals(role);
    }
}