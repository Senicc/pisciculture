<?php
require_once "db.php";
require_once "utils.php";

if ($_SERVER["REQUEST_METHOD"] !== "POST") {
    fail("Method not allowed", 405);
}

$body = json_input();
$id = intval($body["id_poisson"] ?? 0);
$quantite = intval($body["quantite"] ?? 0);
$date = trim($body["date_introduction"] ?? "");
$poids = floatval($body["poids_moyen"] ?? 0);
$mortalite = intval($body["mortalite"] ?? 0);
$id_espece = intval($body["id_espece"] ?? 0);
$id_bassin = intval($body["id_bassin"] ?? 0);

if ($id <= 0 || $quantite < 0 || $id_espece <= 0 || $id_bassin <= 0) {
    fail("Invalid parameters for poisson");
}

$stmt = $pdo->prepare("
    INSERT INTO POISSON (ID_poisson, Quantite, Date_introduction, Poids_moyen, Mortalite, ID_espece, ID_bassin) 
    VALUES (?, ?, ?, ?, ?, ?, ?)
    ON DUPLICATE KEY UPDATE 
        Quantite = VALUES(Quantite),
        Date_introduction = VALUES(Date_introduction),
        Poids_moyen = VALUES(Poids_moyen),
        Mortalite = VALUES(Mortalite),
        ID_espece = VALUES(ID_espece),
        ID_bassin = VALUES(ID_bassin)
");

$stmt->execute([$id, $quantite, $date ?: null, $poids, $mortalite, $id_espece, $id_bassin]);

ok(["success" => true, "id_poisson" => $id]);
?>
