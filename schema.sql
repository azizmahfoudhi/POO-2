-- ============================================================
-- Système de Gestion des Clubs de l'IHEC
-- Script de création de la base de données (PostgreSQL / Supabase)
-- ============================================================

-- Table des budgets
CREATE TABLE IF NOT EXISTS budget (
    id BIGSERIAL PRIMARY KEY,
    solde DOUBLE PRECISION NOT NULL DEFAULT 0
);

-- Table des clubs
CREATE TABLE IF NOT EXISTS club (
    id BIGSERIAL PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    type VARCHAR(255),
    budget_id BIGINT REFERENCES budget(id) ON DELETE CASCADE
);

-- Table des membres (héritage SINGLE_TABLE)
-- La colonne 'role' sert de discriminateur : admin, president, etudiant
CREATE TABLE IF NOT EXISTS membre (
    id BIGSERIAL PRIMARY KEY,
    role VARCHAR(31) NOT NULL,
    nom VARCHAR(255),
    prenom VARCHAR(255),
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255)
);

-- Table de jointure clubs <-> membres (relation N:N)
CREATE TABLE IF NOT EXISTS club_membres (
    club_id BIGINT NOT NULL REFERENCES club(id) ON DELETE CASCADE,
    membre_id BIGINT NOT NULL REFERENCES membre(id) ON DELETE CASCADE,
    PRIMARY KEY (club_id, membre_id)
);

-- Table des activités / événements (héritage SINGLE_TABLE)
-- La colonne 'type_evenement' sert de discriminateur
CREATE TABLE IF NOT EXISTS activite (
    id BIGSERIAL PRIMARY KEY,
    type_evenement VARCHAR(31) NOT NULL,
    titre VARCHAR(255),
    date VARCHAR(20),
    duree INTEGER,
    lieu VARCHAR(255),
    statut VARCHAR(20) NOT NULL DEFAULT 'APPROUVE',
    -- Champs spécifiques aux sous-classes
    cout_global DOUBLE PRECISION,       -- EvenementAcademique
    cout_equip DOUBLE PRECISION,        -- EvenementSportif
    cout_log DOUBLE PRECISION,          -- EvenementSportif
    cout_deco DOUBLE PRECISION,         -- EvenementCulturel
    cout_audio DOUBLE PRECISION,        -- EvenementCulturel
    explication TEXT,                    -- EvenementProposition
    -- Relations
    club_id BIGINT REFERENCES club(id) ON DELETE CASCADE,
    proposeur_id BIGINT REFERENCES membre(id) ON DELETE SET NULL
);

-- ============================================================
-- Données initiales
-- ============================================================

-- Budgets
INSERT INTO budget (solde) VALUES (9500);   -- Enactus (10000 - 500 pour Conf IA)
INSERT INTO budget (solde) VALUES (8000);   -- Lions Club
INSERT INTO budget (solde) VALUES (5450);   -- Art Revolution (6000 - 550 pour Concert)

-- Clubs
INSERT INTO club (nom, type, budget_id) VALUES ('Enactus', 'Entrepreneuriat', 1);
INSERT INTO club (nom, type, budget_id) VALUES ('Lions Club', 'Service', 2);
INSERT INTO club (nom, type, budget_id) VALUES ('Art Revolution', 'Culture', 3);

-- Admin
INSERT INTO membre (role, nom, prenom, email, password)
VALUES ('admin', 'Admin', 'Systeme', 'admin@ihec.ucar.tn', 'admin123');

-- Présidents
INSERT INTO membre (role, nom, prenom, email, password)
VALUES ('president', 'Ali', 'Ben Salah', 'ali@ihec.ucar.tn', 'ali123');
INSERT INTO membre (role, nom, prenom, email, password)
VALUES ('president', 'Sara', 'Ben Ali', 'sara@ihec.ucar.tn', 'sara123');
INSERT INTO membre (role, nom, prenom, email, password)
VALUES ('president', 'Leila', 'Ben Youssef', 'leila@ihec.ucar.tn', 'leila123');

-- Association présidents -> clubs
INSERT INTO club_membres (club_id, membre_id) VALUES (1, 2); -- Ali -> Enactus
INSERT INTO club_membres (club_id, membre_id) VALUES (2, 3); -- Sara -> Lions
INSERT INTO club_membres (club_id, membre_id) VALUES (3, 4); -- Leila -> Art

-- Événements initiaux
INSERT INTO activite (type_evenement, titre, date, duree, lieu, statut, cout_global, club_id)
VALUES ('academique', 'Conf IA', '15/12/2025', 3, 'Amphi A', 'APPROUVE', 500, 1);

INSERT INTO activite (type_evenement, titre, date, duree, lieu, statut, cout_deco, cout_audio, club_id)
VALUES ('culturel', 'Concert', '20/12/2025', 2, 'Salle C', 'APPROUVE', 550, 0, 3);
