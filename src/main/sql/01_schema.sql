
DROP DATABASE IF EXISTS flow;
CREATE DATABASE flow;
USE flow;

CREATE TABLE UTILISATEUR(
   id_utilisateur INT AUTO_INCREMENT,
   role VARCHAR(50)  NOT NULL,
   pseudo VARCHAR(50) NOT NULL,
   email VARCHAR(100) NOT NULL,
   mdp VARCHAR(255) NOT NULL,
   date_inscription DATE NOT NULL DEFAULT (CURRENT_DATE),
   PRIMARY KEY(id_utilisateur),
   UNIQUE(email)
);

CREATE TABLE CATEGORIE(
   id_categorie INT AUTO_INCREMENT,
   nom VARCHAR(50) NOT NULL,
   PRIMARY KEY(id_categorie)
);

CREATE TABLE SOURCE(
   id_source INT AUTO_INCREMENT,
   nom VARCHAR(50) NOT NULL,
   PRIMARY KEY(id_source)
);

CREATE TABLE DEVISE(
   id_devise INT AUTO_INCREMENT,
   code VARCHAR(3) NOT NULL,
   PRIMARY KEY(id_devise),
   UNIQUE(code)
);

CREATE TABLE COMPTE(
   id_compte INT AUTO_INCREMENT,
   nom VARCHAR(50) NOT NULL,
   type VARCHAR(50) NOT NULL,
   montant_initial DECIMAL(15,2) NOT NULL DEFAULT 0,
   id_devise INT NOT NULL,
   id_utilisateur INT NOT NULL,
   PRIMARY KEY(id_compte),
   FOREIGN KEY(id_devise) REFERENCES DEVISE(id_devise),
   FOREIGN KEY(id_utilisateur) REFERENCES UTILISATEUR(id_utilisateur)
);

CREATE TABLE TRANSACTIONS(
   id_transaction INT AUTO_INCREMENT,
   nom VARCHAR(50) NOT NULL,
   description VARCHAR(100),
   type VARCHAR(50) NOT NULL,
   montant DECIMAL(15,2) NOT NULL,
   date_transaction DATE NOT NULL,
   id_compte INT NOT NULL,
   id_source INT,
   PRIMARY KEY(id_transaction),
   FOREIGN KEY(id_compte) REFERENCES COMPTE(id_compte),
   FOREIGN KEY(id_source) REFERENCES SOURCE(id_source)
);

CREATE TABLE Classer_depense(
   id_categorie INT,
   id_transaction INT,
   montant_affecte DECIMAL(15,2) NOT NULL,
   PRIMARY KEY(id_categorie, id_transaction),
   FOREIGN KEY(id_categorie) REFERENCES CATEGORIE(id_categorie),
   FOREIGN KEY(id_transaction) REFERENCES TRANSACTIONS(id_transaction)
);

ALTER TABLE UTILISATEUR
    ADD CONSTRAINT ck_utilisateur_role CHECK (role IN ('USER', 'ADMIN')),
    ADD CONSTRAINT ck_utilisateur_pseudo CHECK (TRIM(pseudo) <> ''),
    ADD CONSTRAINT ck_utilisateur_email CHECK (email LIKE '_%@_%._%');

ALTER TABLE CATEGORIE
    ADD CONSTRAINT uq_categorie_nom UNIQUE (nom),
    ADD CONSTRAINT ck_categorie_nom CHECK (TRIM(nom) <> '');

ALTER TABLE SOURCE
    ADD CONSTRAINT uq_source_nom UNIQUE (nom),
    ADD CONSTRAINT ck_source_nom CHECK (TRIM(nom) <> '');

ALTER TABLE DEVISE
    MODIFY code VARCHAR(3) COLLATE utf8mb4_bin NOT NULL,
    ADD CONSTRAINT ck_devise_code CHECK (code REGEXP '^[A-Z]{3}$');

ALTER TABLE COMPTE
    ADD CONSTRAINT ck_compte_nom CHECK (TRIM(nom) <> ''),
    ADD CONSTRAINT ck_compte_type CHECK (type IN ('COURANT', 'EPARGNE', 'ESPECES')),
    ADD CONSTRAINT ck_compte_montant_initial CHECK (montant_initial >= 0);

ALTER TABLE TRANSACTIONS
    ADD CONSTRAINT ck_transactions_nom CHECK (TRIM(nom) <> ''),
    ADD CONSTRAINT ck_transactions_type CHECK (type IN ('REVENU', 'DEPENSE')),
    ADD CONSTRAINT ck_transactions_montant CHECK (montant > 0),
    ADD CONSTRAINT ck_transactions_source CHECK (
           (type = 'REVENU' AND id_source IS NOT NULL)
        OR (type = 'DEPENSE' AND id_source IS NULL)
    ),
    ADD INDEX idx_transactions_compte_date (id_compte, date_transaction);

ALTER TABLE Classer_depense
    ADD CONSTRAINT ck_classer_depense_montant CHECK (montant_affecte > 0);