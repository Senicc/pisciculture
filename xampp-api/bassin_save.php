<?php
require_once "db.php";
require_once "utils.php";

if ($_SERVER["REQUEST_METHOD"] !== "POST") {
    fail("Method not allowed", 405);
}

$body = json_input();
$id = intval($body["id"] ?? 0);
$nom = trim($body["nom"] ?? "");
$capacite = intval($body["capacite"] ?? 0);
$type = trim($body["type"] ?? "");
$localisation = trim($body["localisation"] ?? "");
$etat = trim($body["etat"] ?? "actif");

if ($nom === "" || $capacite <= 0) {
    fail("nom and capacite are required");
}

if ($id > 0) {
    $stmt = $pdo->prepare("UPDATE BASSIN SET Nom_bassin=?, Capacite=?, Type_bassin=?, Localisation=?, Etat=? WHERE ID_bassin=?");
    $stmt->execute([$nom, $capacite, $type ?: null, $localisation ?: null, $etat, $id]);
    ok(["updated" => $stmt->rowCount()]);
}

$stmt = $pdo->prepare("INSERT INTO BASSIN (Nom_bassin, Capacite, Type_bassin, Localisation, Etat) VALUES (?, ?, ?, ?, ?)");
$stmt->execute([$nom, $capacite, $type ?: null, $localisation ?: null, $etat]);
ok(["id" => intval($pdo->lastInsertId())]);
?>
