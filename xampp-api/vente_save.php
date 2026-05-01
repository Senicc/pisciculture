<?php
require_once "db.php";
require_once "utils.php";

if ($_SERVER["REQUEST_METHOD"] !== "POST") {
    fail("Method not allowed", 405);
}

$body = json_input();
$id = intval($body["id"] ?? 0);
$client = trim($body["client"] ?? "");
$prix_total = floatval($body["prix_total"] ?? 0);
$date_vente = trim($body["date_vente"] ?? date("Y-m-d"));
$id_recolte = intval($body["id_recolte"] ?? 0);

if ($client === "" || $prix_total <= 0 || $id_recolte <= 0) {
    fail("client, prix_total and id_recolte are required");
}

if ($id > 0) {
    $stmt = $pdo->prepare("UPDATE VENTE SET Client=?, Prix_total=?, Date_vente=?, ID_recolte=? WHERE ID_vente=?");
    $stmt->execute([$client, $prix_total, $date_vente, $id_recolte, $id]);
    ok(["updated" => $stmt->rowCount()]);
}

$stmt = $pdo->prepare("INSERT INTO VENTE (Client, Prix_total, Date_vente, ID_recolte) VALUES (?, ?, ?, ?)");
$stmt->execute([$client, $prix_total, $date_vente, $id_recolte]);
ok(["id" => intval($pdo->lastInsertId())]);
?>
