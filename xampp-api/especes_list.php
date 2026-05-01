<?php
require_once "db.php";
require_once "utils.php";

$sql = "SELECT ID_espece as id_espece, Nom_espece as nom_espece, 
        Description as description, Prix_unitaire as prix_unitaire 
        FROM ESPECE ORDER BY ID_espece DESC";
$stmt = $pdo->query($sql);
ok($stmt->fetchAll());
?>
