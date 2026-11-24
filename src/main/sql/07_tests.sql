SET NAMES utf8mb4;
USE flow;

SELECT '1. Volume des données' AS etape;

SELECT (SELECT COUNT(*) FROM UTILISATEUR) AS utilisateurs,
       (SELECT COUNT(*) FROM COMPTE) AS comptes,
       (SELECT COUNT(*) FROM TRANSACTIONS) AS transactions,
       (SELECT COUNT(*) FROM Classer_depense) AS repartitions,
       (SELECT COUNT(*) FROM mv_tableau_bord) AS lignes_vue_materialisee;


SELECT '2. Contrôles d''intégrité' AS etape;

SELECT COUNT(*) AS comptes_en_negatif
FROM (
         SELECT c.id_compte,
                c.montant_initial + COALESCE(SUM(CASE WHEN t.type = 'REVENU' THEN t.montant
                                                      ELSE -t.montant END), 0) AS solde
         FROM COMPTE c
                  LEFT JOIN TRANSACTIONS t ON t.id_compte = c.id_compte
         GROUP BY c.id_compte, c.montant_initial
     ) soldes
WHERE solde < 0;

SELECT COUNT(*) AS depenses_sur_reparties
FROM (
         SELECT t.id_transaction
         FROM TRANSACTIONS t
                  JOIN Classer_depense v ON v.id_transaction = t.id_transaction
         GROUP BY t.id_transaction, t.montant
         HAVING SUM(v.montant_affecte) > t.montant
     ) x;


SELECT '3. Cloisonnement par utilisateur (vues + @id_utilisateur)' AS etape;

SET @id_utilisateur = 1;
SELECT 'alex' AS session,
       (SELECT COUNT(*) FROM v_mes_comptes) AS comptes_visibles,
       (SELECT COUNT(*) FROM v_mes_transactions) AS transactions_visibles;

SET @id_utilisateur = 2;
SELECT 'camille' AS session,
       (SELECT COUNT(*) FROM v_mes_comptes) AS comptes_visibles,
       (SELECT COUNT(*) FROM v_mes_transactions) AS transactions_visibles;

SET @id_utilisateur = NULL;
SELECT 'aucun utilisateur' AS session,
       (SELECT COUNT(*) FROM v_mes_comptes) AS comptes_visibles,
       (SELECT COUNT(*) FROM v_mes_transactions) AS transactions_visibles;


SELECT '4. Fonction calculer_solde_compte' AS etape;

SET @id_utilisateur = 1;
SELECT c.id_compte,
       c.nom,
       calculer_solde_compte(c.id_compte) AS solde_fonction,
       c.montant_initial + COALESCE((SELECT SUM(CASE WHEN t.type = 'REVENU' THEN t.montant
                                                     ELSE -t.montant END)
                                     FROM TRANSACTIONS t
                                     WHERE t.id_compte = c.id_compte), 0) AS solde_recalcule
FROM v_mes_comptes c;


SELECT '5. Requête complexe : dépenses par catégorie sur 3 mois' AS etape;

SET @id_utilisateur = 1;
SELECT cat.nom AS categorie,
       COUNT(DISTINCT v.id_transaction) AS nb_depenses,
       SUM(v.montant_affecte) AS total,
       ROUND(100 * SUM(v.montant_affecte) / (
           SELECT SUM(v2.montant_affecte)
           FROM v_mes_repartitions v2
                    JOIN v_mes_transactions t2 ON t2.id_transaction = v2.id_transaction
           WHERE t2.date_transaction >= DATE_SUB(CURRENT_DATE, INTERVAL 3 MONTH)
       ), 1) AS part_pourcent
FROM v_mes_repartitions v
         JOIN v_mes_transactions t ON t.id_transaction = v.id_transaction
         JOIN CATEGORIE cat ON cat.id_categorie = v.id_categorie
WHERE t.date_transaction >= DATE_SUB(CURRENT_DATE, INTERVAL 3 MONTH)
GROUP BY cat.nom
HAVING SUM(v.montant_affecte) > 20
ORDER BY total DESC;


SELECT '6. Index idx_transactions_compte_date : sans puis avec' AS etape;

ANALYZE TABLE TRANSACTIONS;

EXPLAIN ANALYZE
SELECT id_transaction, date_transaction, nom, montant
FROM TRANSACTIONS IGNORE INDEX (idx_transactions_compte_date)
WHERE id_compte = 9
  AND date_transaction BETWEEN '2026-10-01' AND '2026-10-31'
ORDER BY date_transaction;

EXPLAIN ANALYZE
SELECT id_transaction, date_transaction, nom, montant
FROM TRANSACTIONS
WHERE id_compte = 9
  AND date_transaction BETWEEN '2026-10-01' AND '2026-10-31'
ORDER BY date_transaction;


SELECT '7. Procédure enregistrer_depense' AS etape;

SET @id_utilisateur = 1;
SET @solde_avant = calculer_solde_compte(3);

CALL enregistrer_depense(3, 'Test 07', 'Dépense de démonstration', 10.00, CURRENT_DATE,
                         '[{"id_categorie": 1, "montant": 6.00}, {"id_categorie": 9, "montant": 4.00}]',
                         @id_test);

SELECT @id_test AS transaction_creee,
       @solde_avant AS solde_avant,
       calculer_solde_compte(3) AS solde_apres,
       (SELECT COUNT(*) FROM v_mes_repartitions WHERE id_transaction = @id_test) AS lignes_reparties;


SELECT '8. Refus attendus (triggers, procédures, vues)' AS etape;

DROP PROCEDURE IF EXISTS tests_refus;

DELIMITER //

CREATE PROCEDURE tests_refus()
BEGIN
    DECLARE v_refus INT DEFAULT 0;
    DECLARE v_nb_avant INT;
    DECLARE v_revenu INT;
    DECLARE CONTINUE HANDLER FOR SQLEXCEPTION SET v_refus = 1;

    DROP TEMPORARY TABLE IF EXISTS resultats_tests;
    CREATE TEMPORARY TABLE resultats_tests (
        test     VARCHAR(100),
        resultat VARCHAR(30)
    );

    SET @id_utilisateur = 5;
    SET v_refus = 0;
CALL enregistrer_depense(8, 'Console', NULL, 5000.00, CURRENT_DATE, '[]', @x);
INSERT INTO resultats_tests VALUES ('Découvert : dépense de 5000 € sur le compte de Hugo',
                                    CASE WHEN v_refus = 1 THEN 'OK : refusé' ELSE 'ECHEC : accepté' END);

SELECT id_transaction INTO v_revenu
FROM v_mes_transactions
WHERE type = 'REVENU'
ORDER BY montant DESC
    LIMIT 1;

SET v_refus = 0;
CALL supprimer_transaction(v_revenu);
INSERT INTO resultats_tests VALUES ('Découvert : suppression du plus gros revenu de Hugo',
                                    CASE WHEN v_refus = 1 THEN 'OK : refusé' ELSE 'ECHEC : accepté' END);

SET @id_utilisateur = 1;
SELECT COUNT(*) INTO v_nb_avant FROM v_mes_transactions;

SET v_refus = 0;
CALL enregistrer_depense(3, 'Mal répartie', NULL, 10.00, CURRENT_DATE,
                             '[{"id_categorie": 1, "montant": 15.00}]', @x);
INSERT INTO resultats_tests VALUES ('Répartition supérieure au montant de la dépense',
                                    CASE WHEN v_refus = 1 THEN 'OK : refusé' ELSE 'ECHEC : accepté' END);

SET v_refus = 0;
CALL enregistrer_depense(3, 'Doublon', NULL, 10.00, CURRENT_DATE,
                             '[{"id_categorie": 1, "montant": 3}, {"id_categorie": 1, "montant": 3}]', @x);
INSERT INTO resultats_tests VALUES ('Même catégorie deux fois dans une répartition',
                                    CASE WHEN v_refus = 1 THEN 'OK : refusé' ELSE 'ECHEC : accepté' END);

INSERT INTO resultats_tests VALUES ('Atomicité : aucune transaction laissée par les refus',
                                    CASE WHEN (SELECT COUNT(*) FROM v_mes_transactions) = v_nb_avant
                                             THEN 'OK : rien enregistré' ELSE 'ECHEC' END);

SET v_refus = 0;
CALL enregistrer_depense(4, 'Intrusion', NULL, 1.00, CURRENT_DATE, '[]', @x);
INSERT INTO resultats_tests VALUES ('Dépense sur le compte d''un autre utilisateur',
                                    CASE WHEN v_refus = 1 THEN 'OK : refusé' ELSE 'ECHEC : accepté' END);

SET v_refus = 0;
    SET @solde_camille = calculer_solde_compte(4);
INSERT INTO resultats_tests VALUES ('Lecture du solde d''un compte d''un autre utilisateur',
                                    CASE WHEN v_refus = 1 THEN 'OK : refusé' ELSE 'ECHEC : accepté' END);

SELECT id_transaction INTO v_revenu
FROM v_mes_transactions
WHERE type = 'REVENU'
ORDER BY date_transaction DESC
    LIMIT 1;

SET v_refus = 0;
CALL affecter_categorie(v_revenu, 1, 5.00);
INSERT INTO resultats_tests VALUES ('Répartition d''un revenu',
                                    CASE WHEN v_refus = 1 THEN 'OK : refusé' ELSE 'ECHEC : accepté' END);

SET v_refus = 0;
CALL modifier_transaction(@id_test, 'Test 07', NULL, 5.00, NULL, NULL);
INSERT INTO resultats_tests VALUES ('Montant d''une dépense inférieur au total réparti',
                                    CASE WHEN v_refus = 1 THEN 'OK : refusé' ELSE 'ECHEC : accepté' END);

SET v_refus = 0;
UPDATE v_mes_comptes SET montant_initial = montant_initial + 1000 WHERE id_compte = 1;
INSERT INTO resultats_tests VALUES ('Montant initial d''un compte qui a des transactions',
                                    CASE WHEN v_refus = 1 THEN 'OK : refusé' ELSE 'ECHEC : accepté' END);

SET v_refus = 0;
INSERT INTO v_mes_comptes (nom, type, montant_initial, id_devise, id_utilisateur)
VALUES ('Intrus', 'COURANT', 0, 1, 2);
INSERT INTO resultats_tests VALUES ('WITH CHECK OPTION : compte créé pour un autre utilisateur',
                                    CASE WHEN v_refus = 1 THEN 'OK : refusé' ELSE 'ECHEC : accepté' END);

SET v_refus = 0;
UPDATE v_mes_comptes SET id_utilisateur = 2 WHERE id_compte = 1;
INSERT INTO resultats_tests VALUES ('Changement de propriétaire d''un compte',
                                    CASE WHEN v_refus = 1 THEN 'OK : refusé' ELSE 'ECHEC : accepté' END);

SELECT * FROM resultats_tests;

DROP TEMPORARY TABLE resultats_tests;
END //

DELIMITER ;

CALL tests_refus();

DROP PROCEDURE tests_refus;


SELECT '9. Vue matérialisée : avant et après rafraîchissement' AS etape;

SET @id_utilisateur = 1;
SET @mois = DATE_FORMAT(CURRENT_DATE, '%Y-%m-01');

SELECT 'avant' AS moment, rubrique, total, nb_operations, rafraichi_le
FROM v_mon_tableau_bord
WHERE mois = @mois AND type = 'DEPENSE' AND rubrique IN ('Alimentation', 'Maison');

CALL rafraichir_tableau_bord();

SELECT 'après' AS moment, rubrique, total, nb_operations, rafraichi_le
FROM v_mon_tableau_bord
WHERE mois = @mois AND type = 'DEPENSE' AND rubrique IN ('Alimentation', 'Maison');


SELECT '10. Rôles et privilèges' AS etape;

SELECT user, host, account_locked
FROM mysql.user
WHERE user LIKE 'flow%'
ORDER BY user;

SHOW GRANTS FOR 'flow_role_auth';
SHOW GRANTS FOR 'flow_role_utilisateur';
SHOW GRANTS FOR 'flow_role_admin';


SELECT '11. Nettoyage' AS etape;

SET @id_utilisateur = 1;
CALL supprimer_transaction(@id_test);
CALL rafraichir_tableau_bord();
SET @id_utilisateur = NULL;

SELECT COUNT(*) AS transactions_de_test_restantes
FROM TRANSACTIONS
WHERE nom = 'Test 07';