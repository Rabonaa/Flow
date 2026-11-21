
DROP DATABASE IF EXISTS flow;
CREATE DATABASE flow;
USE flow;

CREATE TABLE UTILISATEUR(
   id_utilisateur INT AUTO_INCREMENT,
   role VARCHAR(50),
   pseudo VARCHAR(50),
   email VARCHAR(100),
   mdp VARCHAR(255),
   date_inscription DATE,
   PRIMARY KEY(id_utilisateur),
   UNIQUE(email)
);

CREATE TABLE CATEGORIE(
   id_categorie INT AUTO_INCREMENT,
   nom VARCHAR(50),
   PRIMARY KEY(id_categorie)
);

CREATE TABLE SOURCE(
   id_source INT AUTO_INCREMENT,
   nom VARCHAR(50),
   PRIMARY KEY(id_source)
);

CREATE TABLE DEVISE(
   id_devise INT AUTO_INCREMENT,
   code VARCHAR(3),
   PRIMARY KEY(id_devise),
   UNIQUE(code)
);

CREATE TABLE COMPTE(
   id_compte INT AUTO_INCREMENT,
   nom VARCHAR(50),
   type VARCHAR(50),
   montant_initial DECIMAL(15,2),
   id_devise INT NOT NULL,
   id_utilisateur INT NOT NULL,
   PRIMARY KEY(id_compte),
   FOREIGN KEY(id_devise) REFERENCES DEVISE(id_devise),
   FOREIGN KEY(id_utilisateur) REFERENCES UTILISATEUR(id_utilisateur)
);

CREATE TABLE TRANSACTIONS(
   id_transaction INT AUTO_INCREMENT,
   nom VARCHAR(50),
   description VARCHAR(100),
   type VARCHAR(50),
   montant DECIMAL(15,2),
   date_transaction DATE,
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
   FOREIGN KEY(id_transaction) REFERENCES TRANSACTIONS(id_transaction)
);
