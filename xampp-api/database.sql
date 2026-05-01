CREATE DATABASE IF NOT EXISTS pisciculture CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE pisciculture;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS VENTE;
DROP TABLE IF EXISTS RECOLTE;
DROP TABLE IF EXISTS TRAITEMENT;
DROP TABLE IF EXISTS GERER;
DROP TABLE IF EXISTS EMPLOYE;
DROP TABLE IF EXISTS QUALITE_EAU;
DROP TABLE IF EXISTS NOURRISSAGE;
DROP TABLE IF EXISTS ALIMENTATION;
DROP TABLE IF EXISTS POISSON;
DROP TABLE IF EXISTS ESPECE;
DROP TABLE IF EXISTS BASSIN;
DROP TABLE IF EXISTS UTILISATEUR;
SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE UTILISATEUR (
    ID_user INT AUTO_INCREMENT PRIMARY KEY,
    Username VARCHAR(100) NOT NULL UNIQUE,
    Password VARCHAR(255) NOT NULL,
    Role VARCHAR(50) NOT NULL,
    Nom VARCHAR(150) NULL
);

CREATE TABLE BASSIN (
    ID_bassin INT AUTO_INCREMENT PRIMARY KEY,
    Nom_bassin VARCHAR(120) NOT NULL,
    Capacite INT NOT NULL,
    Type_bassin VARCHAR(80) NULL,
    Localisation VARCHAR(150) NULL,
    Etat VARCHAR(30) DEFAULT 'actif'
);

CREATE TABLE ESPECE (
    ID_espece INT AUTO_INCREMENT PRIMARY KEY,
    Nom_espece VARCHAR(120) NOT NULL,
    Description TEXT NULL
);

CREATE TABLE POISSON (
    ID_poisson INT AUTO_INCREMENT PRIMARY KEY,
    Quantite INT NOT NULL,
    Date_introduction DATE NULL,
    Poids_moyen DECIMAL(10,2) NULL,
    Mortalite INT DEFAULT 0,
    ID_espece INT NULL,
    ID_bassin INT NULL,
    CONSTRAINT fk_poisson_espece FOREIGN KEY (ID_espece) REFERENCES ESPECE(ID_espece),
    CONSTRAINT fk_poisson_bassin FOREIGN KEY (ID_bassin) REFERENCES BASSIN(ID_bassin)
);

CREATE TABLE ALIMENTATION (
    ID_aliment INT AUTO_INCREMENT PRIMARY KEY,
    Nom_aliment VARCHAR(120) NOT NULL,
    Type_aliment VARCHAR(80) NULL,
    Stock DECIMAL(10,2) DEFAULT 0
);

CREATE TABLE NOURRISSAGE (
    ID_nourrissage INT AUTO_INCREMENT PRIMARY KEY,
    Date_nourrissage DATE NULL,
    Quantite DECIMAL(10,2) NOT NULL,
    ID_bassin INT NULL,
    ID_aliment INT NULL,
    CONSTRAINT fk_nourrissage_bassin FOREIGN KEY (ID_bassin) REFERENCES BASSIN(ID_bassin),
    CONSTRAINT fk_nourrissage_aliment FOREIGN KEY (ID_aliment) REFERENCES ALIMENTATION(ID_aliment)
);

CREATE TABLE QUALITE_EAU (
    ID_qualite INT AUTO_INCREMENT PRIMARY KEY,
    Temperature DECIMAL(5,2) NULL,
    Ph DECIMAL(4,2) NULL,
    Oxygene DECIMAL(5,2) NULL,
    Date_mesure DATE NULL,
    ID_bassin INT NULL,
    CONSTRAINT fk_qualite_bassin FOREIGN KEY (ID_bassin) REFERENCES BASSIN(ID_bassin)
);

CREATE TABLE EMPLOYE (
    ID_employe INT AUTO_INCREMENT PRIMARY KEY,
    Nom VARCHAR(100) NOT NULL,
    Prenom VARCHAR(100) NULL,
    Role VARCHAR(80) NULL,
    Telephone VARCHAR(30) NULL
);

CREATE TABLE GERER (
    ID_employe INT NOT NULL,
    ID_bassin INT NOT NULL,
    PRIMARY KEY (ID_employe, ID_bassin),
    CONSTRAINT fk_gerer_employe FOREIGN KEY (ID_employe) REFERENCES EMPLOYE(ID_employe),
    CONSTRAINT fk_gerer_bassin FOREIGN KEY (ID_bassin) REFERENCES BASSIN(ID_bassin)
);

CREATE TABLE TRAITEMENT (
    ID_traitement INT AUTO_INCREMENT PRIMARY KEY,
    Description TEXT NULL,
    Date_traitement DATE NULL,
    ID_bassin INT NULL,
    CONSTRAINT fk_traitement_bassin FOREIGN KEY (ID_bassin) REFERENCES BASSIN(ID_bassin)
);

CREATE TABLE RECOLTE (
    ID_recolte INT AUTO_INCREMENT PRIMARY KEY,
    Date_recolte DATE NULL,
    Quantite INT NOT NULL,
    Poids_total DECIMAL(10,2) NOT NULL,
    ID_bassin INT NULL,
    CONSTRAINT fk_recolte_bassin FOREIGN KEY (ID_bassin) REFERENCES BASSIN(ID_bassin)
);

CREATE TABLE VENTE (
    ID_vente INT AUTO_INCREMENT PRIMARY KEY,
    Client VARCHAR(150) NOT NULL,
    Prix_total DECIMAL(12,2) NOT NULL,
    Date_vente DATE NULL,
    ID_recolte INT NULL,
    CONSTRAINT fk_vente_recolte FOREIGN KEY (ID_recolte) REFERENCES RECOLTE(ID_recolte)
);

INSERT INTO UTILISATEUR (Username, Password, Role, Nom) VALUES
('admin', 'admin', 'admin', 'Administrateur'),
('employe', 'employe', 'employe', 'Employe'),
('user', 'user', 'user', 'Utilisateur Standard');

INSERT INTO BASSIN (Nom_bassin, Capacite, Type_bassin, Localisation, Etat) VALUES
('Bassin Nord', 600, 'Beton', 'Zone A', 'actif'),
('Bassin Sud', 450, 'Terre', 'Zone B', 'actif');

INSERT INTO ESPECE (Nom_espece, Description) VALUES
('Tilapia', 'Espece d elevage tres courante'),
('Carpe', 'Espece robuste en bassin');

INSERT INTO ALIMENTATION (Nom_aliment, Type_aliment, Stock) VALUES
('Granule croissance', 'Granule', 120.00),
('Farine proteinee', 'Farine', 80.00);
