-- Erstelle die Datenbank, falls nicht vorhanden
CREATE DATABASE IF NOT EXISTS game_database;

-- Erstelle die Tabelle 'user'
CREATE TABLE IF NOT EXISTS game_user (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    token VARCHAR(255)
);
