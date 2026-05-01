<?php
require_once "db.php";
require_once "utils.php";

$sql = "SELECT ID_poisson as id_poisson, Quantite as quantite, 
        Date_introduction as date_introduction, Poids_moyen as poids_moyen,
        Mortalite as mortalite, ID_espece as id_espece, ID_bassin as id_bassin
        FROM POISSON ORDER BY ID_poisson DESC";
$stmt = $pdo->query($sql);
ok($stmt->fetchAll());
?>
