<?php
require_once "db.php";
require_once "utils.php";

$sql = "SELECT ID_vente as id_vente, Client as client, Quantite as quantite,
        ID_espece as id_espece, Prix_unitaire as prix_unitaire, 
        Prix_total as prix_total, Date_vente as date_vente
        FROM VENTE ORDER BY ID_vente DESC";
$stmt = $pdo->query($sql);
ok($stmt->fetchAll());
?>
