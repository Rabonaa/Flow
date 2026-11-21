
USE flow;

-- ==========================================
-- 1. UTILISATEURS
-- ==========================================
-- Mots de passe NULL pour les comptes fictifs.
-- L'authentification sera geree ulterieurement.

INSERT INTO UTILISATEUR
(role, pseudo, email, mdp, date_inscription)
VALUES
('USER', 'alex', 'alex@flow.example', NULL, '2026-10-01'),
('USER', 'camille', 'camille@flow.example', NULL, '2026-10-15');


-- ==========================================
-- 2. CATEGORIES
-- ==========================================

INSERT INTO CATEGORIE (nom)
VALUES
('Alimentation'),
('Logement'),
('Transport'),
('Abonnements'),
('Restaurants'),
('Sante'),
('Education'),
('Shopping'),
('Maison'),
('Loisirs'),
('Assurances');


-- ==========================================
-- 3. SOURCES DE REVENUS
-- ==========================================

INSERT INTO SOURCE (nom)
VALUES
('Salaire'),
('Bourse'),
('Remboursement'),
('Vente occasion'),
('Cadeau'),
('Interets bancaires');


-- ==========================================
-- 4. DEVISES
-- ==========================================

INSERT INTO DEVISE (code)
VALUES
('EUR'),
('USD'),
('GBP');


-- ==========================================
-- 5. COMPTES
-- ==========================================
-- Utilisateur 1 : Alex
-- Utilisateur 2 : Camille
-- Devise 1 : EUR

INSERT INTO COMPTE
(nom, type, montant_initial, id_devise, id_utilisateur)
VALUES
('Compte courant Alex', 'COURANT', 1200.00, 1, 1),
('Livret epargne Alex', 'EPARGNE', 3500.00, 1, 1),
('Especes Alex', 'ESPECES', 60.00, 1, 1),
('Compte courant Camille', 'COURANT', 900.00, 1, 2);


-- ==========================================
-- 6. TRANSACTIONS
-- ==========================================
-- REVENU : entree d'argent
-- DEPENSE : sortie d'argent
-- Les montants sont toujours positifs.
-- id_source est NULL pour les depenses.

INSERT INTO TRANSACTIONS
(nom, description, type, montant,
 date_transaction, id_compte, id_source)
VALUES

-- Revenus d'Alex
('Salaire novembre', 'Salaire mensuel',
 'REVENU', 1700.00, '2026-11-02', 1, 1),

('Bourse novembre', 'Bourse etudiante',
 'REVENU', 280.00, '2026-11-03', 1, 2),

-- Depenses d'Alex
('Loyer', 'Loyer du mois de novembre',
 'DEPENSE', 620.00, '2026-11-04', 1, NULL),

('Courses supermarche', 'Alimentation et entretien',
 'DEPENSE', 78.90, '2026-11-05', 1, NULL),

('Abonnement transport', 'Transports en commun',
 'DEPENSE', 32.00, '2026-11-06', 1, NULL),

('Netflix', 'Abonnement mensuel',
 'DEPENSE', 9.99, '2026-11-07', 1, NULL),

('Restaurant', 'Repas entre amis',
 'DEPENSE', 24.50, '2026-11-08', 1, NULL),

('Pharmacie', 'Produits de sante',
 'DEPENSE', 15.30, '2026-11-09', 1, NULL),

('Livre Java', 'Livre de programmation',
 'DEPENSE', 35.00, '2026-11-10', 1, NULL),

('Assurance habitation', 'Cotisation mensuelle',
 'DEPENSE', 18.80, '2026-11-11', 1, NULL),

-- Revenu exceptionnel
('Vente casque audio', 'Vente occasion',
 'REVENU', 120.00, '2026-11-12', 1, 4),

-- Depense en especes
('Cafe', 'Achat en especes',
 'DEPENSE', 3.50, '2026-11-13', 3, NULL),

-- Revenus du livret
('Interets livret', 'Interets bancaires',
 'REVENU', 3.25, '2026-11-14', 2, 6),

-- Revenus et depenses de Camille
('Salaire Camille', 'Salaire mensuel',
 'REVENU', 1900.00, '2026-11-02', 4, 1),

('Loyer Camille', 'Location appartement',
 'DEPENSE', 680.00, '2026-11-05', 4, NULL),

('Courses Camille', 'Courses alimentaires',
 'DEPENSE', 55.45, '2026-11-06', 4, NULL),

('Cadeau anniversaire', 'Cadeau familial',
 'REVENU', 50.00, '2026-11-15', 4, 5),

-- Autre depense d'Alex
('Vetements', 'Achat de vetements',
 'DEPENSE', 49.90, '2026-11-16', 1, NULL);


-- ==========================================
-- 7. CLASSIFICATION DES DEPENSES
-- ==========================================
-- Cle primaire composee :
-- (id_categorie, id_transaction)
--
-- Les identifiants correspondent aux insertions
-- precedentes dans la base initialement vide.

INSERT INTO Classer_depense
(id_categorie, id_transaction, montant_affecte)
VALUES

-- Loyer d'Alex
(2, 3, 620.00),

-- Courses reparties sur deux categories
(1, 4, 61.00),
(9, 4, 17.90),

-- Transport
(3, 5, 32.00),

-- Abonnements
(4, 6, 9.99),

-- Restaurant
(5, 7, 24.50),

-- Sante
(6, 8, 15.30),

-- Education
(7, 9, 35.00),

-- Assurance
(11, 10, 18.80),

-- Cafe
(5, 12, 3.50),

-- Loyer de Camille
(2, 15, 680.00),

-- Courses de Camille
(1, 16, 55.45),

-- Vetements
(8, 18, 49.90);

SELECT * FROM UTILISATEUR;
SELECT * FROM CATEGORIE;
SELECT * FROM SOURCE;
SELECT * FROM DEVISE;
SELECT * FROM COMPTE;
SELECT * FROM TRANSACTIONS;
SELECT * FROM Classer_depense;