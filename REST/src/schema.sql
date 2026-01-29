-- schema.sql

-- 1. Einstellungen
PRAGMA foreign_keys = ON;

-- 2. Tabellen löschen (Optional, falls du immer frisch starten willst - hier auskommentiert)
-- DROP TABLE IF EXISTS bookings;
DROP TABLE IF EXISTS offers;
-- DROP TABLE IF EXISTS users;

-- 3. Tabelle Users
CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT UNIQUE,
    display_name TEXT
);

-- 4. Tabelle Offers
CREATE TABLE IF NOT EXISTS offers (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    owner_id INTEGER NOT NULL,
    lat REAL,
    lon REAL,
    address TEXT,
    price TEXT,
    beds INTEGER,
    start_date TEXT DEFAULT '1994-09-03',
    end_date TEXT DEFAULT '1997-09-29',
    has_sauna INTEGER DEFAULT 0,
    has_fireplace INTEGER DEFAULT 0,
    is_smoker INTEGER DEFAULT 0,
    has_pets INTEGER DEFAULT 0,
    has_internet INTEGER DEFAULT 0,
    is_published INTEGER DEFAULT 0,
    FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 5. Dummy Daten (Nur einfügen, wenn die ID noch nicht existiert)
-- User 1 (DU)
INSERT OR IGNORE INTO users (id, username, display_name) VALUES (1, 'me', 'Anbieter');
-- User 2 (Jemand anders)
INSERT OR IGNORE INTO users (id, username, display_name) VALUES (2, 'other', 'Surfer Joe');


-- Angebot 1: Hvide Sande
INSERT OR IGNORE INTO offers (
    id, owner_id, address, lat, lon, price, beds,
    has_sauna, has_fireplace, has_internet, is_published,
    start_date, end_date
);
-- Angebot 1: Hvide Sande
INSERT OR IGNORE INTO offers (id, owner_id, address, lat, lon, price, beds, has_sauna, has_fireplace, has_internet, is_published)
VALUES (1, 1, 'Skallevej 5-1', 55.0292782981621, 10.252526199503954, '25 EUR', 2, 1, 1, 1, 1);

-- Angebot 2: Søndervig (Entwurf)
INSERT OR IGNORE INTO offers (id, owner_id, address, lat, lon, price, beds, has_fireplace, is_published)
VALUES (2, 1, 'Hovedvejen 7', 55.01858308630746, 10.29474200067352, 'Kiste Bier', 4, 1, 0);

-- Angebot 3: Klitmøller (Fremd)
INSERT OR IGNORE INTO offers (id, owner_id, address, lat, lon, price, beds, has_sauna, is_published)
VALUES (3, 2, 'Nakkevej 6-10', 55.016732245051784, 10.336843865717123, '50 EUR', 1, 1, 1);
-- Angebot 4:
INSERT OR IGNORE INTO offers (id, owner_id, address, lat, lon, price, beds, has_sauna, is_published)
VALUES (4, 1, 'Schloss', 55.176228697115505, 10.489189259834449, 'König seien', 1, 1, 1);
