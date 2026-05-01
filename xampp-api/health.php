<?php
header("Content-Type: application/json");
require_once "db.php";
echo json_encode([
  "ok" => true,
  "service" => "pisciculture_api",
  "mysql" => "connected"
]);
?>
