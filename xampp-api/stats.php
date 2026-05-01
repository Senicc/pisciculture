<?php
require_once "db.php";
require_once "utils.php";

$stats = [];

// === Bassins ===
$stats["total_bassins"] = intval($pdo->query("SELECT COUNT(*) FROM BASSIN")->fetchColumn());

// Capacité totale et stock total
$row = $pdo->query("SELECT COALESCE(SUM(Capacite),0) as cap FROM BASSIN")->fetch();
$capaciteTotale = intval($row["cap"]);

// === Poissons ===
$stats["total_poissons"] = intval($pdo->query("SELECT COALESCE(SUM(Quantite),0) FROM POISSON")->fetchColumn());

// Stock en kg = SUM((Quantite - Mortalite) * Poids_moyen / 1000)
$stockKg = $pdo->query("SELECT COALESCE(SUM((Quantite - Mortalite) * Poids_moyen / 1000), 0) FROM POISSON WHERE Poids_moyen IS NOT NULL AND Poids_moyen > 0")->fetchColumn();
$stats["stock_total_kg"] = round(floatval($stockKg), 2);

// Poids moyen global en g
$totalPoissons = $stats["total_poissons"];
$stats["poids_moyen_g"] = ($totalPoissons > 0 && $stockKg > 0)
    ? round(($stockKg * 1000) / $totalPoissons, 1)
    : 0.0;

// Mortalité totale
$stats["mortalite_totale"] = intval($pdo->query("SELECT COALESCE(SUM(Mortalite),0) FROM POISSON")->fetchColumn());

// Taux de remplissage global
$stockTotalPoissons = intval($pdo->query("SELECT COALESCE(SUM(Quantite),0) FROM POISSON")->fetchColumn());
$stats["taux_remplissage"] = ($capaciteTotale > 0)
    ? round(($stockTotalPoissons / $capaciteTotale) * 100, 1)
    : 0.0;

// Bassins avec au moins un poisson
$bassinsRemplis = intval($pdo->query("SELECT COUNT(DISTINCT ID_bassin) FROM POISSON WHERE Quantite > 0")->fetchColumn());
$stats["bassins_remplis"] = $bassinsRemplis;

// === Récoltes ===
$stats["total_recoltes"] = intval($pdo->query("SELECT COUNT(*) FROM RECOLTE")->fetchColumn());
$stats["total_poissons_recoltes"] = intval($pdo->query("SELECT COALESCE(SUM(Quantite),0) FROM RECOLTE")->fetchColumn());
$stats["poids_total_recoltes"] = floatval($pdo->query("SELECT COALESCE(SUM(Poids_total),0) FROM RECOLTE")->fetchColumn());

// === Ventes ===
$stats["total_ventes"] = intval($pdo->query("SELECT COUNT(*) FROM VENTE")->fetchColumn());
$stats["total_revenus"] = floatval($pdo->query("SELECT COALESCE(SUM(Prix_total),0) FROM VENTE")->fetchColumn());

// === Qualité eau ===
$qualite = $pdo->query("SELECT AVG(Temperature), AVG(Ph), AVG(Oxygene) FROM QUALITE_EAU")->fetch(PDO::FETCH_NUM);
$stats["avg_temperature"] = round(floatval($qualite[0] ?? 0), 1);
$stats["avg_ph"] = round(floatval($qualite[1] ?? 0), 2);
$stats["avg_oxygene"] = round(floatval($qualite[2] ?? 0), 1);

ok($stats);
?>
