<?php
require_once "db.php";
require_once "utils.php";

if ($_SERVER["REQUEST_METHOD"] !== "POST") {
    fail("Method not allowed", 405);
}

$body = json_input();
$username = trim($body["username"] ?? "");
$password = trim($body["password"] ?? "");
$nom = trim($body["nom"] ?? "");

if ($username === "" || $password === "") {
    fail("Username and password are required");
}

// Vérifier si l'utilisateur existe déjà
$stmt = $pdo->prepare("SELECT COUNT(*) FROM UTILISATEUR WHERE Username = ?");
$stmt->execute([$username]);
if ($stmt->fetchColumn() > 0) {
    fail("Ce nom d'utilisateur est déjà pris", 409);
}

// Hacher le mot de passe
$hashedPassword = password_hash($password, PASSWORD_BCRYPT);
$role = "user"; // Rôle par défaut

$stmt = $pdo->prepare("INSERT INTO UTILISATEUR (Username, Password, Role, Nom) VALUES (?, ?, ?, ?)");
$stmt->execute([$username, $hashedPassword, $role, $nom ?: null]);

ok([
    "id_user" => intval($pdo->lastInsertId()),
    "username" => $username,
    "role" => $role,
    "nom" => $nom
]);
?>
