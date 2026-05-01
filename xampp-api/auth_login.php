<?php
require_once "db.php";
require_once "utils.php";

if ($_SERVER["REQUEST_METHOD"] !== "POST") {
    fail("Method not allowed", 405);
}

$body = json_input();
$username = trim($body["username"] ?? "");
$password = trim($body["password"] ?? "");

if ($username === "" || $password === "") {
    fail("username and password are required");
}

$stmt = $pdo->prepare("SELECT ID_user, Role, Nom, Password FROM UTILISATEUR WHERE Username = ? LIMIT 1");
$stmt->execute([$username]);
$row = $stmt->fetch();

if (!$row) {
    fail("Identifiants invalides", 401);
}

// Vérification du mot de passe : bcrypt OU fallback texte clair (legacy)
$storedPassword = $row["Password"];
$passwordValid = false;

if (strlen($storedPassword) >= 60 && $storedPassword[0] === '$') {
    // Mot de passe hashé (bcrypt)
    $passwordValid = password_verify($password, $storedPassword);
} else {
    // Legacy : comparaison texte clair + mise à jour automatique vers bcrypt
    if ($password === $storedPassword) {
        $passwordValid = true;
        // Mettre à jour vers bcrypt
        $newHash = password_hash($password, PASSWORD_BCRYPT);
        $pdo->prepare("UPDATE UTILISATEUR SET Password = ? WHERE ID_user = ?")
            ->execute([$newHash, $row["ID_user"]]);
    }
}

if (!$passwordValid) {
    fail("Identifiants invalides", 401);
}

ok([
    "role"     => $row["Role"],
    "username" => $username,
    "nom"      => $row["Nom"] ?? $username,
    "id_user"  => $row["ID_user"]
]);
?>
