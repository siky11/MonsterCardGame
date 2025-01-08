

-- Erstelle die Tabelle 'user'
CREATE TABLE game_user (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    elo INT NOT NULL DEFAULT 1000,        -- Elo-Score, Standardwert ist 1000
    games_played INT NOT NULL DEFAULT 0, -- Anzahl der gespielten Spiele, Standardwert ist 0
    coins INT NOT NULL DEFAULT 20,       -- Coins, Standardwert ist 20
    bio VARCHAR(255) NOT NULL,
    image VARCHAR(255) NOT NULL,
    token VARCHAR(255)                   -- Optionales Feld für Token
);

--Erstellt die Tabelle 'package'
CREATE TABLE game_package (
    package_id UUID PRIMARY KEY DEFAULT gen_random_uuid(), -- Eindeutige Paket-ID
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP -- Zeitpunkt der Paketerstellung
);

-- Tabelle für Karten
CREATE TABLE game_card (
    card_id UUID PRIMARY KEY , -- Eindeutige Karten-ID
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
    FOREIGN KEY (package_id) REFERENCES game_package(package_id) ON DELETE CASCADE -- sorgt dafür das packages aus der entsprechenden Tabelle kommen
);

CREATE TABLE user_stack (
    user_id UUID NOT NULL,              -- Referenz auf einen Benutzer
    card_id UUID NOT NULL,              -- Referenz auf eine Karte
    PRIMARY KEY (user_id, card_id)    -- Jede Karte gehört nur einmal zum Stack eines Benutzers
);

-- Tabelle für Package Transaktionen
CREATE TABLE package_transactions (
    transaction_id UUID PRIMARY KEY DEFAULT gen_random_uuid(), -- Eindeutige Transaktions-ID
    user_id UUID NOT NULL,                                     -- Referenz auf den Benutzer
    package_id UUID NOT NULL,                                  -- Referenz auf das Paket
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,      -- Zeitstempel der Transaktion
    FOREIGN KEY (user_id) REFERENCES game_user(id) ON DELETE CASCADE,
    FOREIGN KEY (package_id) REFERENCES game_package(package_id) ON DELETE CASCADE
);

CREATE TABLE user_deck (
    user_id UUID REFERENCES game_user (id),
    card_id UUID REFERENCES game_card (card_id),
    PRIMARY KEY (user_id, card_id)
);

