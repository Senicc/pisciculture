<?php
require_once "db.php";
require_once "utils.php";

$sql = "SELECT ID_recolte as id_recolte, Date_recolte as date_recolte, 
        Quantite as quantite, Poids_total as poids_total, ID_bassin as id_bassin
        FROM RECOLTE ORDER BY ID_recolte DESC";
$stmt = $pdo->query($sql);
ok($stmt->fetchAll());
?>
