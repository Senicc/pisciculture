<?php
require_once "db.php";
require_once "utils.php";

$sql = "SELECT ID_bassin as id_bassin, Nom_bassin as nom_bassin, 
        Capacite as capacite, Type_bassin as type_bassin, 
        Localisation as localisation, Etat as etat
        FROM BASSIN ORDER BY ID_bassin DESC";
$stmt = $pdo->query($sql);
ok($stmt->fetchAll());
?>
