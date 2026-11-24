SET NAMES utf8mb4;
USE flow;

CREATE USER IF NOT EXISTS 'flow_owner'@'localhost' IDENTIFIED BY 'FlowOwner#Verrouille2026' ACCOUNT LOCK;
ALTER USER 'flow_owner'@'localhost' ACCOUNT LOCK;
GRANT ALL PRIVILEGES ON flow.* TO 'flow_owner'@'localhost';

DROP FUNCTION IF EXISTS uid_session;

CREATE DEFINER = 'flow_owner'@'localhost' FUNCTION uid_session()
    RETURNS INT
    NOT DETERMINISTIC
    NO SQL
    SQL SECURITY DEFINER
    RETURN @id_utilisateur;

CREATE OR REPLACE
    DEFINER = 'flow_owner'@'localhost'
    SQL SECURITY DEFINER
VIEW v_mes_comptes AS
SELECT id_compte, nom, type, montant_initial, id_devise, id_utilisateur
FROM COMPTE
WHERE id_utilisateur = uid_session()
    WITH CASCADED CHECK OPTION;

CREATE OR REPLACE
    DEFINER = 'flow_owner'@'localhost'
    SQL SECURITY DEFINER
VIEW v_mes_transactions AS
SELECT t.id_transaction, t.nom, t.description, t.type, t.montant,
       t.date_transaction, t.id_compte, t.id_source
FROM TRANSACTIONS t
         JOIN COMPTE c ON c.id_compte = t.id_compte
WHERE c.id_utilisateur = uid_session();

CREATE OR REPLACE
    DEFINER = 'flow_owner'@'localhost'
    SQL SECURITY DEFINER
VIEW v_mes_repartitions AS
SELECT v.id_categorie, v.id_transaction, v.montant_affecte
FROM Classer_depense v
         JOIN TRANSACTIONS t ON t.id_transaction = v.id_transaction
         JOIN COMPTE c ON c.id_compte = t.id_compte
WHERE c.id_utilisateur = uid_session();

CREATE OR REPLACE
    DEFINER = 'flow_owner'@'localhost'
    SQL SECURITY DEFINER
VIEW v_historique AS
SELECT t.id_transaction,
       t.date_transaction,
       t.type,
       t.nom,
       t.description,
       t.montant,
       c.id_compte,
       c.nom AS compte,
       d.code AS devise,
       s.nom AS source,
       GROUP_CONCAT(cat.nom ORDER BY v.montant_affecte DESC SEPARATOR ', ') AS categories,
       COALESCE(SUM(v.montant_affecte), 0) AS montant_classe,
       CASE WHEN t.type = 'DEPENSE'
                THEN t.montant - COALESCE(SUM(v.montant_affecte), 0)
            ELSE 0
           END AS montant_non_classe
FROM TRANSACTIONS t
         JOIN COMPTE c ON c.id_compte = t.id_compte
         JOIN DEVISE d ON d.id_devise = c.id_devise
         LEFT JOIN SOURCE s ON s.id_source = t.id_source
         LEFT JOIN Classer_depense v ON v.id_transaction = t.id_transaction
         LEFT JOIN CATEGORIE cat ON cat.id_categorie = v.id_categorie
WHERE c.id_utilisateur = uid_session()
GROUP BY t.id_transaction, c.id_compte, d.code, s.nom;

CREATE OR REPLACE
    DEFINER = 'flow_owner'@'localhost'
    SQL SECURITY DEFINER
VIEW v_calcul_tableau_bord AS
SELECT c.id_utilisateur,
       DATE_FORMAT(t.date_transaction, '%Y-%m-01') AS mois,
       d.code AS devise,
       'REVENU' AS type,
       s.nom AS rubrique,
       SUM(t.montant) AS total,
       COUNT(*) AS nb_operations
FROM TRANSACTIONS t
         JOIN COMPTE c ON c.id_compte = t.id_compte
         JOIN DEVISE d ON d.id_devise = c.id_devise
         JOIN SOURCE s ON s.id_source = t.id_source
WHERE t.type = 'REVENU'
GROUP BY c.id_utilisateur, mois, d.code, s.nom
UNION ALL
SELECT c.id_utilisateur,
       DATE_FORMAT(t.date_transaction, '%Y-%m-01'),
       d.code,
       'DEPENSE',
       cat.nom,
       SUM(v.montant_affecte),
       COUNT(*)
FROM Classer_depense v
         JOIN TRANSACTIONS t ON t.id_transaction = v.id_transaction
         JOIN COMPTE c ON c.id_compte = t.id_compte
         JOIN DEVISE d ON d.id_devise = c.id_devise
         JOIN CATEGORIE cat ON cat.id_categorie = v.id_categorie
GROUP BY c.id_utilisateur, DATE_FORMAT(t.date_transaction, '%Y-%m-01'), d.code, cat.nom
UNION ALL
SELECT c.id_utilisateur,
       DATE_FORMAT(t.date_transaction, '%Y-%m-01'),
       d.code,
       'DEPENSE',
       'Non classé',
       SUM(t.montant - COALESCE(x.reparti, 0)),
       COUNT(*)
FROM TRANSACTIONS t
         JOIN COMPTE c ON c.id_compte = t.id_compte
         JOIN DEVISE d ON d.id_devise = c.id_devise
         LEFT JOIN (
    SELECT id_transaction, SUM(montant_affecte) AS reparti
    FROM Classer_depense
    GROUP BY id_transaction
) x ON x.id_transaction = t.id_transaction
WHERE t.type = 'DEPENSE'
  AND t.montant > COALESCE(x.reparti, 0)
GROUP BY c.id_utilisateur, DATE_FORMAT(t.date_transaction, '%Y-%m-01'), d.code;

DROP TABLE IF EXISTS mv_tableau_bord;

CREATE TABLE mv_tableau_bord (
 id_utilisateur  INT            NOT NULL,
 mois            DATE           NOT NULL,
 devise          VARCHAR(3)     NOT NULL,
 type            VARCHAR(10)    NOT NULL,
 rubrique        VARCHAR(50)    NOT NULL,
 total           DECIMAL(15,2)  NOT NULL,
 nb_operations   INT            NOT NULL,
 rafraichi_le    DATETIME       NOT NULL,
 CONSTRAINT pk_mv_tableau_bord PRIMARY KEY (id_utilisateur, mois, devise, type, rubrique)
) ENGINE = InnoDB;

INSERT INTO mv_tableau_bord (id_utilisateur, mois, devise, type, rubrique, total, nb_operations, rafraichi_le)
SELECT id_utilisateur, mois, devise, type, rubrique, total, nb_operations, NOW()
FROM v_calcul_tableau_bord;

CREATE OR REPLACE
    DEFINER = 'flow_owner'@'localhost'
    SQL SECURITY DEFINER
VIEW v_mon_tableau_bord AS
SELECT mois, devise, type, rubrique, total, nb_operations, rafraichi_le
FROM mv_tableau_bord
WHERE id_utilisateur = uid_session();

CREATE OR REPLACE
    DEFINER = 'flow_owner'@'localhost'
    SQL SECURITY DEFINER
VIEW v_admin_statistiques AS
SELECT DATE_FORMAT(t.date_transaction, '%Y-%m-01') AS mois,
       COUNT(DISTINCT c.id_utilisateur) AS utilisateurs_actifs,
       COUNT(*) AS nb_transactions,
       SUM(CASE WHEN t.type = 'REVENU' THEN 1 ELSE 0 END) AS nb_revenus,
       SUM(CASE WHEN t.type = 'DEPENSE' THEN 1 ELSE 0 END) AS nb_depenses
FROM TRANSACTIONS t
         JOIN COMPTE c ON c.id_compte = t.id_compte
GROUP BY mois;
