<?php
require_once "db.php";
require_once "utils.php";

if ($_SERVER["REQUEST_METHOD"] !== "POST") {
    fail("Method not allowed", 405);
}

$body = json_input();
$id = intval($body["id"] ?? 0);
if ($id <= 0) {
    fail("id is required");
}

$stmt = $pdo->prepare("DELETE FROM VENTE WHERE ID_vente = ?");
$stmt->execute([$id]);
ok(["deleted" => $stmt->rowCount()]);
?>
