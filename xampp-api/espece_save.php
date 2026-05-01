<?php
require_once "db.php";
require_once "utils.php";

if ($_SERVER["REQUEST_METHOD"] !== "POST") {
    fail("Method not allowed", 405);
}

$body = json_input();
$id = intval($body["id_espece"] ?? 0);
$nom = trim($body["nom_espece"] ?? "");
$desc = trim($body["description"] ?? "");
$prix = floatval($body["prix_unitaire"] ?? 0);

if ($id <= 0 || $nom === "") {
    fail("id_espece and nom_espece are required");
}

/* 
 Note: SQLite "ESPECE" schema might not have Prix_unitaire initially 
 but DataSyncManager sends 'prix_unitaire'. Let's support it if it exists.
*/
try {
    $stmt = $pdo->prepare("
        INSERT INTO ESPECE (ID_espece, Nom_espece, Description) 
        VALUES (?, ?, ?)
        ON DUPLICATE KEY UPDATE 
            Nom_espece = VALUES(Nom_espece),
            Description = VALUES(Description)
    ");
    $stmt->execute([$id, $nom, $desc ?: null]);
} catch (Exception $e) {
    fail("Error saving espece: " . $e->getMessage());
}

ok(["success" => true, "id_espece" => $id]);
?>
