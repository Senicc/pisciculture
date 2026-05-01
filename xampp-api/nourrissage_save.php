<?php
require_once "db.php";
require_once "utils.php";

if ($_SERVER["REQUEST_METHOD"] !== "POST") {
    fail("Method not allowed", 405);
}

$body = json_input();
$id = intval($body["id_nourrissage"] ?? 0);
$date = trim($body["date_nourrissage"] ?? "");
$quantite = floatval($body["quantite"] ?? 0);
$id_bassin = intval($body["id_bassin"] ?? 0);
$id_aliment = intval($body["id_aliment"] ?? 0);

if ($id <= 0 || $quantite <= 0 || $id_bassin <= 0 || $id_aliment <= 0) {
    fail("Invalid parameters for nourrissage");
}

$stmt = $pdo->prepare("
    INSERT INTO NOURRISSAGE (ID_nourrissage, Date_nourrissage, Quantite, ID_bassin, ID_aliment) 
    VALUES (?, ?, ?, ?, ?)
    ON DUPLICATE KEY UPDATE 
        Date_nourrissage = VALUES(Date_nourrissage),
        Quantite = VALUES(Quantite),
        ID_bassin = VALUES(ID_bassin),
        ID_aliment = VALUES(ID_aliment)
");

$stmt->execute([$id, $date ?: null, $quantite, $id_bassin, $id_aliment]);

ok(["success" => true, "id_nourrissage" => $id]);
?>
