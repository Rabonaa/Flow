USE flow;

DROP FUNCTION IF EXISTS calculer_solde_compte;

DELIMITER //
CREATE FUNCTION calculer_solde_compte(p_id_compte INT)
RETURNS DECIMAL(15,2)
NOT DETERMINISTIC
READS SQL DATA
SQL SECURITY INVOKER

BEGIN
    DECLARE v_solde DECIMAL(15,2);

    SELECT
        COALESCE(c.montant_initial, 0) + 
        COALESCE(SUM( CASE
							WHEN t.type = 'REVENU' THEN t.montant
							WHEN t.type = 'DEPENSE' THEN -t.montant
							ELSE 0
							END)
				, 0)
    INTO v_solde

    FROM COMPTE c
    LEFT JOIN TRANSACTIONS t ON c.id_compte = t.id_compte
	WHERE c.id_compte = p_id_compte
	GROUP BY c.id_compte, c.montant_initial;

    IF v_solde IS NULL THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Compte introuvable';
    END IF;

    RETURN v_solde;
END //

DELIMITER ;
