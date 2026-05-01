<?php
require_once "db.php";
require_once "utils.php";

if ($_SERVER["REQUEST_METHOD"] !== "POST") {
    fail("Method not allowed", 405);
}

$body = json_input();
$id = intval($body["id_recolte"] ?? 0);
$date = trim($body["date_recolte"] ?? "");
$quantite = intval($body["quantite"] ?? 0);
$poids = floatval($body["poids_total"] ?? 0);
$id_bassin = intval($body["id_bassin"] ?? 0);

if ($id <= 0 || $quantite <= 0 || $id_bassin <= 0) {
    fail("id_recolte, quantite, and id_bassin are required");
}

$stmt = $pdo->prepare("
    INSERT INTO RECOLTE (ID_recolte, Date_recolte, Quantite, Poids_total, ID_bassin) 
    VALUES (?, ?, ?, ?, ?)
    ON DUPLICATE KEY UPDATE 
        Date_recolte = VALUES(Date_recolte),
        Quantite = VALUES(Quantite),
        Poids_total = VALUES(Poids_total),
        ID_bassin = VALUES(ID_bassin)
");

$stmt->execute([$id, $date ?: null, $quantite, $poids, $id_bassin]);

ok(["success" => true, "id_recolte" => $id]);
?>
