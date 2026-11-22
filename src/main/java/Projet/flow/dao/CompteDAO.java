package Projet.flow.dao;

import Projet.flow.model.Compte;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

/** Ne voit que les comptes de l'utilisateur connecté (vue v_mes_comptes). */
public interface CompteDAO {

    /** Le propriétaire est toujours l'utilisateur connecté, quoi que contienne l'objet. */
    int creer(Compte compte) throws SQLException;

    /** null si le compte n'existe pas ou n'appartient pas à l'utilisateur. */
    Compte trouverParId(int idCompte) throws SQLException;

    List<Compte> listerMesComptes() throws SQLException;

    /** Montant initial et devise refusés par trigger si le compte a des transactions. */
    boolean modifier(Compte compte) throws SQLException;

    /** Refusé si le compte a des transactions. */
    boolean supprimer(int idCompte) throws SQLException;

    BigDecimal calculerSolde(int idCompte) throws SQLException;
}