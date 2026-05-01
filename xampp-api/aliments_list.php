<?php
require_once "db.php";
require_once "utils.php";

$sql = "SELECT ID_aliment as id_aliment, Nom_aliment as nom_aliment, 
        Type_aliment as type_aliment, Stock as stock
        FROM ALIMENTATION ORDER BY ID_aliment DESC";
$stmt = $pdo->query($sql);
ok($stmt->fetchAll());
?>
