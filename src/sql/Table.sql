
DROP DATABASE IF EXISTS flow;
CREATE DATABASE flow;
USE flow;

CREATE TABLE UTILISATEUR(
   id_utilisateur INT,
   role VARCHAR(50),
   pseudo VARCHAR(50),
   email VARCHAR(100),
   mdp VARCHAR(50),
   date_inscription VARCHAR(50),
   PRIMARY KEY(id_utilisateur)
);

CREATE TABLE CATEGORIE(
   id_categorie INT,
   nom VARCHAR(50),
   PRIMARY KEY(id_categorie)
);

CREATE TABLE SOURCE(
   id_source INT,
   nom VARCHAR(50),
   PRIMARY KEY(id_source)
);

CREATE TABLE DEVISE(
   id_devise INT,
   code VARCHAR(3),
   PRIMARY KEY(id_devise)
);

CREATE TABLE COMPTE(
   id_compte INT,
   nom VARCHAR(50),
   type VARCHAR(50),
   montant_initial DECIMAL(15,2),
   id_devise INT NOT NULL,
   id_utilisateur INT NOT NULL,
   PRIMARY KEY(id_compte),
   FOREIGN KEY(id_devise) REFERENCES DEVISE(id_devise),
   FOREIGN KEY(id_utilisateur) REFERENCES UTILISATEUR(id_utilisateur)
);

CREATE TABLE TRANSACTION(
   id_transaction INT,
   nom VARCHAR(50),
   description VARCHAR(100),
   type VARCHAR(50),
   montant DECIMAL(15,2),
   date_ DATE,
   id_compte INT NOT NULL,
   id_source INT,
   PRIMARY KEY(id_transaction),
   FOREIGN KEY(id_compte) REFERENCES COMPTE(id_compte),
   FOREIGN KEY(id_source) REFERENCES SOURCE(id_source)
);

CREATE TABLE Classer_depense(
   id_categorie INT,
   id_transaction INT,
   montant_affecte DECIMAL(15,2),
   PRIMARY KEY(id_categorie, id_transaction),
   FOREIGN KEY(id_categorie) REFERENCES CATEGORIE(id_categorie),
   FOREIGN KEY(id_transaction) REFERENCES TRANSACTION(id_transaction)
);
