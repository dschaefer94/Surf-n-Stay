-- schema.sql

-- 1. Einstellungen
PRAGMA foreign_keys = ON;

-- 2. Tabellen löschen (Optional, falls du immer frisch starten willst - hier auskommentiert)
-- DROP TABLE IF EXISTS bookings;
-- DROP TABLE IF EXISTS offers;
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
    lat REAL NOT NULL,
    lon REAL NOT NULL,
    address TEXT NOT NULL,
    price TEXT NOT NULL,
    beds INTEGER NOT NULL,
    start_date TEXT NOT NULL,
    end_date TEXT NOT NULL,
    has_sauna INTEGER DEFAULT 0,
    has_fireplace INTEGER DEFAULT 0,
    is_smoker INTEGER DEFAULT 0,
    has_pets INTEGER DEFAULT 0,
    has_internet INTEGER DEFAULT 0,
    is_published INTEGER DEFAULT 0,

    FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE,

    UNIQUE (owner_id, address, start_date, end_date)
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
)
-- Angebot 1: Skallevej
INSERT OR IGNORE INTO offers (id, owner_id, address, lat, lon, price, beds, has_sauna, has_fireplace, has_internet, is_published, start_date, end_date)
VALUES (1, 1, 'Skallevej 5-1, Dänemark', 55.0292782981621, 10.252526199503954, '25 EUR', 2, 1, 1, 1, 1, '2026-06-01', '2026-06-14');

-- Angebot 2: Søndervig
INSERT OR IGNORE INTO offers (id, owner_id, address, lat, lon, price, beds, has_sauna, is_published, start_date, end_date)
VALUES (2, 1, 'Hovedvejen 7, Dänemark', 55.01858308630746, 10.29474200067352, 'Kiste Bier', 4, 0, 0, '2026-07-01', '2026-07-31');

-- Angebot 3: Klitmøller
INSERT OR IGNORE INTO offers (id, owner_id, address, lat, lon, price, beds, has_sauna, is_published, start_date, end_date)
VALUES (3, 2, 'Nakkevej 6-10, Dänemark', 55.016732245051784, 10.336843865717123, '50 EUR', 1, 1, 1, '2026-08-15', '2026-08-30');

-- Angebot 4: Schloss
INSERT OR IGNORE INTO offers (id, owner_id, address, lat, lon, price, beds, has_sauna, is_published, start_date, end_date)
VALUES (4, 1, 'Schloss, Dänemark', 55.176228697115505, 10.489189259834449, 'König seien', 1, 1, 1, '2026-01-01', '2026-12-31');
