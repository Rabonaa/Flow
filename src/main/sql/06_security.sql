USE flow;

DROP USER IF EXISTS ''@'localhost', ''@'%';
DROP USER IF EXISTS 'user'@'localhost';

ALTER USER 'flow_owner'@'localhost' ACCOUNT LOCK;

DROP USER IF EXISTS 'flow_auth'@'localhost', 'flow_app_user'@'localhost', 'flow_app_admin'@'localhost';
DROP ROLE IF EXISTS 'flow_role_auth', 'flow_role_utilisateur', 'flow_role_admin';

CREATE ROLE 'flow_role_auth', 'flow_role_utilisateur', 'flow_role_admin';

GRANT EXECUTE ON PROCEDURE flow.lire_identifiants TO 'flow_role_auth';

GRANT SELECT, INSERT, UPDATE, DELETE ON flow.v_mes_comptes TO 'flow_role_utilisateur';
GRANT SELECT ON flow.v_mes_transactions TO 'flow_role_utilisateur';
GRANT SELECT ON flow.v_mes_repartitions TO 'flow_role_utilisateur';
GRANT SELECT ON flow.v_historique TO 'flow_role_utilisateur';
GRANT SELECT ON flow.v_mon_tableau_bord TO 'flow_role_utilisateur';
GRANT SELECT ON flow.CATEGORIE TO 'flow_role_utilisateur';
GRANT SELECT ON flow.SOURCE TO 'flow_role_utilisateur';
GRANT SELECT ON flow.DEVISE TO 'flow_role_utilisateur';
GRANT EXECUTE ON FUNCTION flow.calculer_solde_compte TO 'flow_role_utilisateur';
GRANT EXECUTE ON PROCEDURE flow.enregistrer_revenu TO 'flow_role_utilisateur';
GRANT EXECUTE ON PROCEDURE flow.enregistrer_depense TO 'flow_role_utilisateur';
GRANT EXECUTE ON PROCEDURE flow.modifier_transaction TO 'flow_role_utilisateur';
GRANT EXECUTE ON PROCEDURE flow.supprimer_transaction TO 'flow_role_utilisateur';
GRANT EXECUTE ON PROCEDURE flow.affecter_categorie TO 'flow_role_utilisateur';
GRANT EXECUTE ON PROCEDURE flow.modifier_affectation TO 'flow_role_utilisateur';
GRANT EXECUTE ON PROCEDURE flow.supprimer_affectation TO 'flow_role_utilisateur';

GRANT SELECT (id_utilisateur, role, pseudo, email, date_inscription),
      INSERT (role, pseudo, email, mdp, date_inscription),
UPDATE (role, pseudo, email, mdp),
DELETE
ON flow.UTILISATEUR TO 'flow_role_admin';
GRANT SELECT, INSERT, UPDATE, DELETE ON flow.CATEGORIE TO 'flow_role_admin';
GRANT SELECT, INSERT, UPDATE, DELETE ON flow.SOURCE TO 'flow_role_admin';
GRANT SELECT, INSERT, UPDATE, DELETE ON flow.DEVISE TO 'flow_role_admin';
GRANT SELECT ON flow.v_admin_statistiques TO 'flow_role_admin';
GRANT EXECUTE ON PROCEDURE flow.rafraichir_tableau_bord TO 'flow_role_admin';

CREATE USER 'flow_auth'@'localhost' IDENTIFIED BY 'FlowAuth#2026';
CREATE USER 'flow_app_user'@'localhost' IDENTIFIED BY 'FlowUser#2026';
CREATE USER 'flow_app_admin'@'localhost' IDENTIFIED BY 'FlowAdmin#2026';

GRANT 'flow_role_auth' TO 'flow_auth'@'localhost';
GRANT 'flow_role_utilisateur' TO 'flow_app_user'@'localhost';
GRANT 'flow_role_admin' TO 'flow_app_admin'@'localhost';

SET DEFAULT ROLE 'flow_role_auth' TO 'flow_auth'@'localhost';
SET DEFAULT ROLE 'flow_role_utilisateur' TO 'flow_app_user'@'localhost';
SET DEFAULT ROLE 'flow_role_admin' TO 'flow_app_admin'@'localhost';

FLUSH PRIVILEGES;