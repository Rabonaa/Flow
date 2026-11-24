SET NAMES utf8mb4;
USE flow;

DROP FUNCTION IF EXISTS calculer_solde_compte;
DROP PROCEDURE IF EXISTS lire_identifiants;
DROP PROCEDURE IF EXISTS enregistrer_revenu;
DROP PROCEDURE IF EXISTS enregistrer_depense;
DROP PROCEDURE IF EXISTS modifier_transaction;
DROP PROCEDURE IF EXISTS supprimer_transaction;
DROP PROCEDURE IF EXISTS affecter_categorie;
DROP PROCEDURE IF EXISTS modifier_affectation;
DROP PROCEDURE IF EXISTS supprimer_affectation;
DROP PROCEDURE IF EXISTS rafraichir_tableau_bord;

DELIMITER //

CREATE DEFINER = 'flow_owner'@'localhost' FUNCTION calculer_solde_compte(p_id_compte INT)
RETURNS DECIMAL(15,2)
NOT DETERMINISTIC
READS SQL DATA
SQL SECURITY DEFINER
BEGIN
    DECLARE v_solde DECIMAL(15,2);

SELECT
    COALESCE(c.montant_initial, 0) +
    COALESCE(SUM(CASE
                     WHEN t.type = 'REVENU' THEN t.montant
                     WHEN t.type = 'DEPENSE' THEN -t.montant
                     ELSE 0
        END), 0)
INTO v_solde
FROM COMPTE c
         LEFT JOIN TRANSACTIONS t ON c.id_compte = t.id_compte
WHERE c.id_compte = p_id_compte
  AND c.id_utilisateur = uid_session()
GROUP BY c.id_compte, c.montant_initial;

IF v_solde IS NULL THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Compte introuvable';
END IF;

RETURN v_solde;
END //

CREATE DEFINER = 'flow_owner'@'localhost' PROCEDURE lire_identifiants(IN p_email VARCHAR(100))
READS SQL DATA
SQL SECURITY DEFINER
BEGIN
SELECT id_utilisateur, role, pseudo, mdp
FROM UTILISATEUR
WHERE email = p_email;
END //

CREATE DEFINER = 'flow_owner'@'localhost' PROCEDURE enregistrer_revenu(
    IN  p_id_compte      INT,
    IN  p_nom            VARCHAR(50),
    IN  p_description    VARCHAR(100),
    IN  p_montant        DECIMAL(15,2),
    IN  p_date           DATE,
    IN  p_id_source      INT,
    OUT p_id_transaction INT
)
MODIFIES SQL DATA
SQL SECURITY DEFINER
BEGIN
    DECLARE v_trouve INT DEFAULT 0;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
BEGIN
ROLLBACK;
RESIGNAL;
END;

START TRANSACTION;

SELECT COUNT(*) INTO v_trouve
FROM COMPTE
WHERE id_compte = p_id_compte
  AND id_utilisateur = uid_session()
    FOR UPDATE;

IF v_trouve = 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Compte introuvable';
END IF;

    IF p_id_source IS NULL THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Un revenu doit avoir une source';
END IF;

INSERT INTO TRANSACTIONS (nom, description, type, montant, date_transaction, id_compte, id_source)
VALUES (p_nom, p_description, 'REVENU', p_montant, COALESCE(p_date, CURRENT_DATE), p_id_compte, p_id_source);

SET p_id_transaction = LAST_INSERT_ID();

COMMIT;
END //

CREATE DEFINER = 'flow_owner'@'localhost' PROCEDURE enregistrer_depense(
    IN  p_id_compte      INT,
    IN  p_nom            VARCHAR(50),
    IN  p_description    VARCHAR(100),
    IN  p_montant        DECIMAL(15,2),
    IN  p_date           DATE,
    IN  p_repartitions   JSON,
    OUT p_id_transaction INT
)
MODIFIES SQL DATA
SQL SECURITY DEFINER
BEGIN
    DECLARE v_trouve INT DEFAULT 0;
    DECLARE v_id INT;
    DECLARE v_nb INT DEFAULT 0;
    DECLARE i INT DEFAULT 0;
    DECLARE v_categorie INT;
    DECLARE v_montant DECIMAL(15,2);

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
BEGIN
ROLLBACK;
RESIGNAL;
END;

START TRANSACTION;

SELECT COUNT(*) INTO v_trouve
FROM COMPTE
WHERE id_compte = p_id_compte
  AND id_utilisateur = uid_session()
    FOR UPDATE;

IF v_trouve = 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Compte introuvable';
END IF;

INSERT INTO TRANSACTIONS (nom, description, type, montant, date_transaction, id_compte, id_source)
VALUES (p_nom, p_description, 'DEPENSE', p_montant, COALESCE(p_date, CURRENT_DATE), p_id_compte, NULL);

SET v_id = LAST_INSERT_ID();

    IF p_repartitions IS NOT NULL THEN
        SET v_nb = JSON_LENGTH(p_repartitions);
END IF;

    WHILE i < v_nb DO
        SET v_categorie = JSON_EXTRACT(p_repartitions, CONCAT('$[', i, '].id_categorie'));
        SET v_montant = JSON_EXTRACT(p_repartitions, CONCAT('$[', i, '].montant'));

        IF v_categorie IS NULL OR v_montant IS NULL THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Chaque ligne de répartition doit avoir une catégorie et un montant';
END IF;

        IF EXISTS (SELECT 1 FROM Classer_depense
                   WHERE id_transaction = v_id AND id_categorie = v_categorie) THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Vous avez choisi deux fois la même catégorie';
END IF;

INSERT INTO Classer_depense (id_categorie, id_transaction, montant_affecte)
VALUES (v_categorie, v_id, v_montant);

SET i = i + 1;
END WHILE;

COMMIT;

SET p_id_transaction = v_id;
END //

CREATE DEFINER = 'flow_owner'@'localhost' PROCEDURE modifier_transaction(
    IN p_id_transaction INT,
    IN p_nom            VARCHAR(50),
    IN p_description    VARCHAR(100),
    IN p_montant        DECIMAL(15,2),
    IN p_date           DATE,
    IN p_id_source      INT
)
MODIFIES SQL DATA
SQL SECURITY DEFINER
BEGIN
    DECLARE v_trouve INT DEFAULT 0;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
BEGIN
ROLLBACK;
RESIGNAL;
END;

START TRANSACTION;

SELECT COUNT(*) INTO v_trouve
FROM TRANSACTIONS t
         JOIN COMPTE c ON c.id_compte = t.id_compte
WHERE t.id_transaction = p_id_transaction
  AND c.id_utilisateur = uid_session()
    FOR UPDATE;

IF v_trouve = 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Transaction introuvable';
END IF;

UPDATE TRANSACTIONS
SET nom = p_nom,
    description = p_description,
    montant = p_montant,
    date_transaction = COALESCE(p_date, date_transaction),
    id_source = CASE WHEN type = 'REVENU' THEN p_id_source ELSE NULL END
WHERE id_transaction = p_id_transaction;

COMMIT;
END //

CREATE DEFINER = 'flow_owner'@'localhost' PROCEDURE supprimer_transaction(IN p_id_transaction INT)
MODIFIES SQL DATA
SQL SECURITY DEFINER
BEGIN
    DECLARE v_trouve INT DEFAULT 0;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
BEGIN
ROLLBACK;
RESIGNAL;
END;

START TRANSACTION;

SELECT COUNT(*) INTO v_trouve
FROM TRANSACTIONS t
         JOIN COMPTE c ON c.id_compte = t.id_compte
WHERE t.id_transaction = p_id_transaction
  AND c.id_utilisateur = uid_session()
    FOR UPDATE;

IF v_trouve = 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Transaction introuvable';
END IF;

DELETE FROM Classer_depense
WHERE id_transaction = p_id_transaction;

DELETE FROM TRANSACTIONS
WHERE id_transaction = p_id_transaction;

COMMIT;
END //

CREATE DEFINER = 'flow_owner'@'localhost' PROCEDURE affecter_categorie(
    IN p_id_transaction INT,
    IN p_id_categorie   INT,
    IN p_montant        DECIMAL(15,2)
)
MODIFIES SQL DATA
SQL SECURITY DEFINER
BEGIN
    DECLARE v_trouve INT DEFAULT 0;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
BEGIN
ROLLBACK;
RESIGNAL;
END;

START TRANSACTION;

SELECT COUNT(*) INTO v_trouve
FROM TRANSACTIONS t
         JOIN COMPTE c ON c.id_compte = t.id_compte
WHERE t.id_transaction = p_id_transaction
  AND c.id_utilisateur = uid_session()
    FOR UPDATE;

IF v_trouve = 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Transaction introuvable';
END IF;

INSERT INTO Classer_depense (id_categorie, id_transaction, montant_affecte)
VALUES (p_id_categorie, p_id_transaction, p_montant);

COMMIT;
END //

CREATE DEFINER = 'flow_owner'@'localhost' PROCEDURE modifier_affectation(
    IN p_id_transaction INT,
    IN p_id_categorie   INT,
    IN p_montant        DECIMAL(15,2)
)
MODIFIES SQL DATA
SQL SECURITY DEFINER
BEGIN
    DECLARE v_trouve INT DEFAULT 0;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
BEGIN
ROLLBACK;
RESIGNAL;
END;

START TRANSACTION;

SELECT COUNT(*) INTO v_trouve
FROM TRANSACTIONS t
         JOIN COMPTE c ON c.id_compte = t.id_compte
WHERE t.id_transaction = p_id_transaction
  AND c.id_utilisateur = uid_session()
    FOR UPDATE;

IF v_trouve = 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Transaction introuvable';
END IF;

    IF NOT EXISTS (SELECT 1 FROM Classer_depense
                   WHERE id_transaction = p_id_transaction AND id_categorie = p_id_categorie) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Affectation introuvable';
END IF;

UPDATE Classer_depense
SET montant_affecte = p_montant
WHERE id_transaction = p_id_transaction
  AND id_categorie = p_id_categorie;

COMMIT;
END //

CREATE DEFINER = 'flow_owner'@'localhost' PROCEDURE supprimer_affectation(
    IN p_id_transaction INT,
    IN p_id_categorie   INT
)
MODIFIES SQL DATA
SQL SECURITY DEFINER
BEGIN
    DECLARE v_trouve INT DEFAULT 0;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
BEGIN
ROLLBACK;
RESIGNAL;
END;

START TRANSACTION;

SELECT COUNT(*) INTO v_trouve
FROM TRANSACTIONS t
         JOIN COMPTE c ON c.id_compte = t.id_compte
WHERE t.id_transaction = p_id_transaction
  AND c.id_utilisateur = uid_session()
    FOR UPDATE;

IF v_trouve = 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Transaction introuvable';
END IF;

    IF NOT EXISTS (SELECT 1 FROM Classer_depense
                   WHERE id_transaction = p_id_transaction AND id_categorie = p_id_categorie) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Affectation introuvable';
END IF;

DELETE FROM Classer_depense
WHERE id_transaction = p_id_transaction
  AND id_categorie = p_id_categorie;

COMMIT;
END //

CREATE DEFINER = 'flow_owner'@'localhost' PROCEDURE rafraichir_tableau_bord()
MODIFIES SQL DATA
SQL SECURITY DEFINER
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
BEGIN
ROLLBACK;
RESIGNAL;
END;

START TRANSACTION;

    DELETE FROM mv_tableau_bord
    WHERE id_utilisateur > 0;

INSERT INTO mv_tableau_bord (id_utilisateur, mois, devise, type, rubrique, total, nb_operations, rafraichi_le)
SELECT id_utilisateur, mois, devise, type, rubrique, total, nb_operations, NOW()
FROM v_calcul_tableau_bord;

COMMIT;
END //

DELIMITER ;