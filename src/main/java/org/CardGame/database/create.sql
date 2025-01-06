

-- Erstelle die Tabelle 'user'
CREATE TABLE game_user (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    token VARCHAR(255)
);

--Erstellt die Tabelle 'package'
CREATE TABLE game_package (
    package_id UUID PRIMARY KEY DEFAULT gen_random_uuid(), -- Eindeutige Paket-ID
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP -- Zeitpunkt der Paketerstellung
);

-- Tabelle für Karten
CREATE TABLE game_card (
    card_id UUID PRIMARY KEY DEFAULT gen_random_uuid(), -- Eindeutige Karten-ID
    name TEXT NOT NULL, -- Name der Karte
    type TEXT NOT NULL, -- Typ der Karte (z. B. "Monster", "Spell")
    element_type TEXT NOT NULL,
    damage INTEGER NOT NULL CHECK (damage >= 0), -- Schaden (muss positiv sein)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP -- Erstellungsdatum der Karte
    );

-- Die Tabelle verknüpft Pakete und Karten (Jede Zeile eine Karte die einem Packet zugeordnet ist)
CREATE TABLE package_cards (
    package_id UUID NOT NULL, -- Referenz auf ein Paket
    card_id UUID NOT NULL, -- Referenz auf eine Karte
    PRIMARY KEY (package_id, card_id), -- verhindert das eine Karte mehrfach demselben Packet zugeordnet wird
    FOREIGN KEY (package_id) REFERENCES game_package(package_id) ON DELETE CASCADE, -- sorgt dafür das packages aus der entsprechenden Tabelle kommen
    FOREIGN KEY (card_id) REFERENCES game_card(card_id) ON DELETE CASCADE -- sorgt dafür das die Karte (on delete Cascade sorgt dafür das die Datenbank automatisch verknüpfte einträge löscht)
);



