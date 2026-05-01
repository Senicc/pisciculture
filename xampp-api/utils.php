<?php
header("Content-Type: application/json; charset=utf-8");

function json_input() {
    $raw = file_get_contents("php://input");
    if (!$raw) return [];
    $data = json_decode($raw, true);
    return is_array($data) ? $data : [];
}

function ok($data = []) {
    echo json_encode(["ok" => true, "data" => $data], JSON_UNESCAPED_UNICODE);
    exit;
}

function fail($message, $code = 400) {
    http_response_code($code);
    echo json_encode(["ok" => false, "error" => $message], JSON_UNESCAPED_UNICODE);
    exit;
}
?>
