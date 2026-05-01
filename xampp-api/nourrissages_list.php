<?php
require_once "db.php";
require_once "utils.php";

$sql = "SELECT ID_nourrissage as id_nourrissage, Date_nourrissage as date_nourrissage, 
        Quantite as quantite, ID_bassin as id_bassin, ID_aliment as id_aliment
        FROM NOURRISSAGE ORDER BY ID_nourrissage DESC";
$stmt = $pdo->query($sql);
ok($stmt->fetchAll());
?>
