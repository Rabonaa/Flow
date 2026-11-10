
USE flow;
-- ==========================================
-- INSERTION DES UTILISATEURS
-- ==========================================

INSERT INTO UTILISATEUR VALUES
(1, 'admin', 'admin_flow', 'admin@flow.test', 'demo_hash_001', '2026-09-01'),
(2, 'user', 'alex', 'alex@flow.test', 'demo_hash_002', '2026-09-10'),
(3, 'user', 'emma', 'emma@flow.test', 'demo_hash_003', '2026-09-15'),
(4, 'user', 'lucas', 'lucas@flow.test', 'demo_hash_004', '2026-10-01'),
(5, 'user', 'sarah', 'sarah@flow.test', 'demo_hash_005', '2026-10-12');

-- ==========================================
-- INSERTION DES CATEGORIES
-- ==========================================

INSERT INTO CATEGORIE VALUES
(1, 'Alimentation'),
(2, 'Logement'),
(3, 'Transport'),
(4, 'Loisirs'),
(5, 'Shopping'),
(6, 'Sante'),
(7, 'Education'),
(8, 'Abonnements'),
(9, 'Voyages'),
(10, 'Autres');

-- ==========================================
-- INSERTION DES SOURCES
-- ==========================================

INSERT INTO SOURCE VALUES
(1, 'Salaire'),
(2, 'Bourse'),
(3, 'Aide familiale'),
(4, 'Freelance'),
(5, 'Vente occasion'),
(6, 'Remboursement'),
(7, 'Investissement'),
(8, 'Autres revenus');

-- ==========================================
-- INSERTION DES DEVISES
-- ==========================================

INSERT INTO DEVISE VALUES
(1, 'EUR'),
(2, 'USD'),
(3, 'GBP'),
(4, 'MGA'),
(5, 'MYR');

-- ==========================================
-- INSERTION DES COMPTES
-- ==========================================

INSERT INTO COMPTE VALUES
(1, 'Compte courant', 'Courant', 1250.00, 1, 2),
(2, 'Livret A', 'Epargne', 3500.00, 1, 2),
(3, 'Compte Revolut', 'Courant', 450.00, 1, 2),
(4, 'Compte principal', 'Courant', 2100.00, 1, 3),
(5, 'Compte epargne', 'Epargne', 6000.00, 1, 3),
(6, 'Compte courant', 'Courant', 850.00, 1, 4),
(7, 'Portefeuille USD', 'Courant', 300.00, 2, 4),
(8, 'Compte principal', 'Courant', 1750.00, 1, 5),
(9, 'Epargne voyage', 'Epargne', 1200.00, 1, 5);

-- ==========================================
-- TRANSACTIONS ALEX
-- ==========================================

INSERT INTO TRANSACTION VALUES
(1, 'Salaire octobre', 'Job etudiant', 'revenu', 850.00, '2026-10-01', 1, 1),
(2, 'Bourse octobre', 'Bourse mensuelle', 'revenu', 450.00, '2026-10-05', 1, 2),
(3, 'Loyer octobre', 'Loyer appartement', 'depense', 600.00, '2026-10-06', 1, NULL),
(4, 'Courses Carrefour', 'Courses alimentaires', 'depense', 85.50, '2026-10-08', 1, NULL),
(5, 'Abonnement Netflix', 'Streaming mensuel', 'depense', 13.49, '2026-10-10', 1, NULL),
(6, 'Tram TBM', 'Abonnement transport', 'depense', 35.00, '2026-10-12', 1, NULL),
(7, 'Restaurant', 'Diner entre amis', 'depense', 42.00, '2026-10-15', 1, NULL),
(8, 'Freelance web', 'Creation site internet', 'revenu', 300.00, '2026-10-18', 1, 4),
(9, 'Vetements', 'Achat chaussures', 'depense', 89.99, '2026-10-20', 1, NULL),
(10, 'Courses Lidl', 'Courses hebdomadaires', 'depense', 56.30, '2026-10-23', 1, NULL),
(11, 'Aide familiale', 'Virement familial', 'revenu', 200.00, '2026-10-25', 1, 3),
(12, 'Cinema', 'Seance du samedi', 'depense', 14.00, '2026-10-27', 1, NULL),
(13, 'Salaire novembre', 'Job etudiant', 'revenu', 920.00, '2026-11-01', 1, 1),
(14, 'Loyer novembre', 'Loyer appartement', 'depense', 600.00, '2026-11-03', 1, NULL),
(15, 'Courses novembre', 'Alimentation', 'depense', 72.80, '2026-11-05', 1, NULL),
(16, 'Spotify', 'Abonnement musique', 'depense', 6.99, '2026-11-06', 1, NULL),
(17, 'Manuels scolaires', 'Livres informatiques', 'depense', 45.00, '2026-11-07', 1, NULL);

-- ==========================================
-- TRANSACTIONS EMMA
-- ==========================================

INSERT INTO TRANSACTION VALUES
(18, 'Salaire octobre', 'Salaire entreprise', 'revenu', 2400.00, '2026-10-01', 4, 1),
(19, 'Loyer', 'Location appartement', 'depense', 850.00, '2026-10-04', 4, NULL),
(20, 'Supermarche', 'Courses mensuelles', 'depense', 160.00, '2026-10-08', 4, NULL),
(21, 'Salle de sport', 'Abonnement fitness', 'depense', 29.99, '2026-10-10', 4, NULL),
(22, 'Essence', 'Plein carburant', 'depense', 65.00, '2026-10-15', 4, NULL),
(23, 'Freelance design', 'Mission graphique', 'revenu', 450.00, '2026-10-20', 4, 4),
(24, 'Weekend Paris', 'Hotel et sorties', 'depense', 280.00, '2026-10-25', 4, NULL),
(25, 'Salaire novembre', 'Salaire entreprise', 'revenu', 2400.00, '2026-11-01', 4, 1),
(26, 'Loyer novembre', 'Location appartement', 'depense', 850.00, '2026-11-04', 4, NULL),
(27, 'Courses', 'Courses alimentaires', 'depense', 95.00, '2026-11-07', 4, NULL);

-- ==========================================
-- TRANSACTIONS LUCAS
-- ==========================================

INSERT INTO TRANSACTION VALUES
(28, 'Bourse octobre', 'Bourse etudiante', 'revenu', 550.00, '2026-10-05', 6, 2),
(29, 'Aide parents', 'Virement parents', 'revenu', 350.00, '2026-10-07', 6, 3),
(30, 'Loyer chambre', 'Logement etudiant', 'depense', 420.00, '2026-10-08', 6, NULL),
(31, 'Courses', 'Achats alimentaires', 'depense', 75.00, '2026-10-12', 6, NULL),
(32, 'Jeu video', 'Achat jeu PC', 'depense', 59.99, '2026-10-18', 6, NULL),
(33, 'Livres', 'Livres de cours', 'depense', 35.00, '2026-10-22', 6, NULL),
(34, 'Vente PC', 'Vente ordinateur ancien', 'revenu', 250.00, '2026-10-28', 6, 5),
(35, 'Bourse novembre', 'Bourse etudiante', 'revenu', 550.00, '2026-11-05', 6, 2),
(36, 'Courses novembre', 'Supermarche', 'depense', 80.00, '2026-11-07', 6, NULL);

-- ==========================================
-- TRANSACTIONS SARAH
-- ==========================================

INSERT INTO TRANSACTION VALUES
(37, 'Salaire octobre', 'Salaire mensuel', 'revenu', 1900.00, '2026-10-01', 8, 1),
(38, 'Loyer', 'Appartement', 'depense', 700.00, '2026-10-05', 8, NULL),
(39, 'Courses', 'Supermarche', 'depense', 125.00, '2026-10-09', 8, NULL),
(40, 'Shopping', 'Achats vetements', 'depense', 120.00, '2026-10-13', 8, NULL),
(41, 'Netflix', 'Abonnement streaming', 'depense', 13.49, '2026-10-15', 8, NULL),
(42, 'Train', 'Billet SNCF', 'depense', 78.00, '2026-10-20', 8, NULL),
(43, 'Remboursement', 'Remboursement achat', 'revenu', 45.00, '2026-10-24', 8, 6),
(44, 'Salaire novembre', 'Salaire mensuel', 'revenu', 1900.00, '2026-11-01', 8, 1),
(45, 'Courses novembre', 'Alimentation', 'depense', 92.00, '2026-11-06', 8, NULL);

-- ==========================================
-- CLASSIFICATION DES DEPENSES
-- ==========================================

INSERT INTO Classer_depense VALUES
-- Alex
(2, 3, 600.00),
(1, 4, 85.50),
(8, 5, 13.49),
(3, 6, 35.00),
(1, 7, 42.00),
(5, 9, 89.99),
(1, 10, 56.30),
(4, 12, 14.00),
(2, 14, 600.00),
(1, 15, 72.80),
(8, 16, 6.99),
(7, 17, 45.00),

-- Emma
(2, 19, 850.00),
(1, 20, 160.00),
(4, 21, 29.99),
(3, 22, 65.00),
(9, 24, 200.00),
(4, 24, 80.00),
(2, 26, 850.00),
(1, 27, 95.00),

-- Lucas
(2, 30, 420.00),
(1, 31, 75.00),
(4, 32, 59.99),
(7, 33, 35.00),
(1, 36, 80.00),

-- Sarah
(2, 38, 700.00),
(1, 39, 125.00),
(5, 40, 120.00),
(8, 41, 13.49),
(3, 42, 78.00),
(1, 45, 92.00);

SELECT 'UTILISATEUR' AS table_nom, COUNT(*) AS total FROM UTILISATEUR
UNION ALL
SELECT 'CATEGORIE', COUNT(*) FROM CATEGORIE
UNION ALL
SELECT 'SOURCE', COUNT(*) FROM SOURCE
UNION ALL
SELECT 'DEVISE', COUNT(*) FROM DEVISE
UNION ALL
SELECT 'COMPTE', COUNT(*) FROM COMPTE
UNION ALL
SELECT 'TRANSACTION', COUNT(*) FROM TRANSACTION
UNION ALL
SELECT 'Classer_depense', COUNT(*) FROM Classer_depense;
