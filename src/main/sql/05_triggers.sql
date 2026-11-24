SET NAMES utf8mb4;
USE flow;

DROP TRIGGER IF EXISTS trg_transactions_avant_insertion;
DROP TRIGGER IF EXISTS trg_transactions_avant_modification;
DROP TRIGGER IF EXISTS trg_transactions_avant_suppression;
DROP TRIGGER IF EXISTS trg_repartitions_avant_insertion;
DROP TRIGGER IF EXISTS trg_repartitions_avant_modification;
DROP TRIGGER IF EXISTS trg_compte_avant_modification;

DELIMITER //

CREATE DEFINER = 'flow_owner'@'localhost' TRIGGER trg_transactions_avant_insertion
BEFORE INSERT ON TRANSACTIONS
FOR EACH ROW
BEGIN
    DECLARE v_solde DECIMAL(15,2);
    DECLARE v_message VARCHAR(255);

    IF NEW.type = 'DEPENSE' THEN
        SET v_solde = calculer_solde_compte(NEW.id_compte);

        IF NEW.montant > v_solde THEN
            SET v_message = CONCAT('Solde insuffisant (solde disponible : ', v_solde, ')');
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = v_message;
END IF;
END IF;
END //

CREATE DEFINER = 'flow_owner'@'localhost' TRIGGER trg_transactions_avant_modification
BEFORE UPDATE ON TRANSACTIONS
                      FOR EACH ROW
BEGIN
    DECLARE v_solde DECIMAL(15,2);
    DECLARE v_reparti DECIMAL(15,2);
    DECLARE v_message VARCHAR(255);

    IF NEW.id_compte <> OLD.id_compte OR NEW.type <> OLD.type THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Le compte et le type d''une transaction ne sont pas modifiables';
END IF;

    SET v_solde = calculer_solde_compte(OLD.id_compte);

    IF OLD.type = 'REVENU' THEN
        SET v_solde = v_solde - OLD.montant + NEW.montant;
ELSE
        SET v_solde = v_solde + OLD.montant - NEW.montant;
END IF;

    IF v_solde < 0 THEN
        SET v_message = CONCAT('Solde insuffisant (le solde passerait à ', v_solde, ')');
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = v_message;
END IF;

    IF NEW.type = 'DEPENSE' THEN
        SET v_reparti = (SELECT COALESCE(SUM(montant_affecte), 0)
                         FROM Classer_depense
                         WHERE id_transaction = OLD.id_transaction);

        IF NEW.montant < v_reparti THEN
            SET v_message = CONCAT('Montant inférieur au total réparti (', v_reparti, ')');
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = v_message;
END IF;
END IF;
END //

CREATE DEFINER = 'flow_owner'@'localhost' TRIGGER trg_transactions_avant_suppression
BEFORE DELETE ON TRANSACTIONS
FOR EACH ROW
BEGIN
    DECLARE v_solde DECIMAL(15,2);
    DECLARE v_message VARCHAR(255);

    IF OLD.type = 'REVENU' THEN
        SET v_solde = calculer_solde_compte(OLD.id_compte) - OLD.montant;

        IF v_solde < 0 THEN
            SET v_message = CONCAT('Solde insuffisant (le solde passerait à ', v_solde, ')');
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = v_message;
END IF;
END IF;
END //

CREATE DEFINER = 'flow_owner'@'localhost' TRIGGER trg_repartitions_avant_insertion
BEFORE INSERT ON Classer_depense
FOR EACH ROW
BEGIN
    DECLARE v_type VARCHAR(50);
    DECLARE v_montant DECIMAL(15,2);
    DECLARE v_deja DECIMAL(15,2);
    DECLARE v_message VARCHAR(255);

    SET v_type = (SELECT type FROM TRANSACTIONS WHERE id_transaction = NEW.id_transaction);
    SET v_montant = (SELECT montant FROM TRANSACTIONS WHERE id_transaction = NEW.id_transaction);

    IF v_type = 'REVENU' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Seule une dépense peut être répartie';
END IF;

    SET v_deja = (SELECT COALESCE(SUM(montant_affecte), 0)
                  FROM Classer_depense
                  WHERE id_transaction = NEW.id_transaction);

    IF v_deja + NEW.montant_affecte > v_montant THEN
                SET v_message = CONCAT('La répartition dépasse le montant de la dépense. Il reste ',
                               v_montant - v_deja, ' à répartir.');
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = v_message;
END IF;
END //

CREATE DEFINER = 'flow_owner'@'localhost' TRIGGER trg_repartitions_avant_modification
BEFORE UPDATE ON Classer_depense
                      FOR EACH ROW
BEGIN
    DECLARE v_montant DECIMAL(15,2);
    DECLARE v_autres DECIMAL(15,2);
    DECLARE v_message VARCHAR(255);

    IF NEW.id_transaction <> OLD.id_transaction THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Une répartition ne peut pas être déplacée vers une autre dépense';
END IF;

    SET v_montant = (SELECT montant FROM TRANSACTIONS WHERE id_transaction = NEW.id_transaction);

    SET v_autres = (SELECT COALESCE(SUM(montant_affecte), 0)
                    FROM Classer_depense
                    WHERE id_transaction = NEW.id_transaction
                      AND id_categorie <> OLD.id_categorie);

    IF v_autres + NEW.montant_affecte > v_montant THEN
        SET v_message = CONCAT('La répartition dépasse le montant de la dépense. Il reste ',
                               v_montant - v_autres, ' à répartir.');
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = v_message;
END IF;
END //

CREATE DEFINER = 'flow_owner'@'localhost' TRIGGER trg_compte_avant_modification
BEFORE UPDATE ON COMPTE
                      FOR EACH ROW
BEGIN
    IF NEW.id_utilisateur <> OLD.id_utilisateur THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Le propriétaire d''un compte n''est pas modifiable';
END IF;

    IF (NEW.montant_initial <> OLD.montant_initial OR NEW.id_devise <> OLD.id_devise)
       AND EXISTS (SELECT 1 FROM TRANSACTIONS WHERE id_compte = OLD.id_compte) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Montant initial et devise non modifiables : le compte a des transactions';
END IF;
END //

DELIMITER ;