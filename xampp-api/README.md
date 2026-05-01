# API MySQL (XAMPP)

Ce dossier contient un exemple minimal pour connecter l'application Android a MySQL via XAMPP.

## Etapes

1. Copier le dossier `xampp-api` vers `C:\xampp\htdocs\pisciculture_api`.
2. Importer `database.sql` dans phpMyAdmin (base `pisciculture`).
3. Adapter les identifiants dans `db.php`.
4. Tester `http://localhost/pisciculture_api/health.php` dans le navigateur.
5. Sur emulateur Android, l'application utilise `http://10.0.2.2/pisciculture_api/`.

## Important

L'application Android ne doit pas se connecter directement a MySQL.
La bonne pratique est de passer par cette API HTTP (PHP) qui parle a MySQL.

## Endpoints disponibles

- `POST /auth_login.php`
- `GET /bassins_list.php`
- `POST /bassin_save.php`
- `GET /recoltes_list.php`
- `GET /ventes_list.php`
- `POST /vente_save.php`
- `POST /vente_delete.php`
- `GET /stats.php`
