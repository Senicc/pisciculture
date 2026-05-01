<?php
require_once "db.php";
require_once "utils.php";

if ($_SERVER["REQUEST_METHOD"] !== "POST") {
    fail("Method not allowed", 405);
}

$body = json_input();
$id = intval($body["id_aliment"] ?? 0);
$nom = trim($body["nom_aliment"] ?? "");
$type = trim($body["type_aliment"] ?? "");
$stock = floatval($body["stock"] ?? 0);

if ($id <= 0 || $nom === "") {
    fail("Invalid parameters for aliment");
}

$stmt = $pdo->prepare("
    INSERT INTO ALIMENTATION (ID_aliment, Nom_aliment, Type_aliment, Stock) 
    VALUES (?, ?, ?, ?)
    ON DUPLICATE KEY UPDATE 
        Nom_aliment = VALUES(Nom_aliment),
        Type_aliment = VALUES(Type_aliment),
        Stock = VALUES(Stock)
");

$stmt->execute([$id, $nom, $type ?: null, $stock]);

ok(["success" => true, "id_aliment" => $id]);
?>
