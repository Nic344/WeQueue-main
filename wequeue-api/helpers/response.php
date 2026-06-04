<?php

declare(strict_types=1);

function sendCorsHeaders(): void
{
    header('Access-Control-Allow-Origin: *');
    header('Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS');
    header('Access-Control-Allow-Headers: Content-Type, Authorization');
    header('Content-Type: application/json; charset=UTF-8');

    if (($_SERVER['REQUEST_METHOD'] ?? '') === 'OPTIONS') {
        http_response_code(200);
        exit;
    }
}

function jsonSuccess(mixed $data = null, string $message = 'OK', int $code = 200): void
{
    sendCorsHeaders();
    http_response_code($code);
    echo json_encode([
        'success' => true,
        'message' => $message,
        'data' => $data,
    ], JSON_UNESCAPED_UNICODE);
    exit;
}

function jsonError(string $message, int $code = 400, mixed $data = null): void
{
    sendCorsHeaders();
    http_response_code($code);
    echo json_encode([
        'success' => false,
        'message' => $message,
        'data' => $data,
    ], JSON_UNESCAPED_UNICODE);
    exit;
}

function getJsonInput(): array
{
    $raw = file_get_contents('php://input');
    if ($raw === false || trim($raw) === '') {
        return [];
    }

    $decoded = json_decode($raw, true);
    if (!is_array($decoded)) {
        jsonError('Invalid JSON body', 400);
    }

    return $decoded;
}

function requireMethod(string $method): void
{
    if (strtoupper($_SERVER['REQUEST_METHOD'] ?? '') !== strtoupper($method)) {
        jsonError('Method not allowed. Expected ' . strtoupper($method), 405);
    }
}

function requireFields(array $input, array $fields): void
{
    $missing = [];
    foreach ($fields as $field) {
        if (!isset($input[$field]) || (is_string($input[$field]) && trim($input[$field]) === '')) {
            $missing[] = $field;
        }
    }
    if ($missing !== []) {
        jsonError('Missing required fields: ' . implode(', ', $missing), 422);
    }
}

function sanitizeUser(array $user): array
{
    unset($user['password']);
    return $user;
}
