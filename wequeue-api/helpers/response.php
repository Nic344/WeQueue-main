<?php

declare(strict_types=1);

ini_set('display_errors', '0');
ini_set('log_errors', '1');

if (!function_exists('initApiResponse')) {
    function initApiResponse(): void
    {
        static $initialized = false;
        if ($initialized) {
            return;
        }
        $initialized = true;

        if (ob_get_level() === 0) {
            ob_start();
        }

        set_exception_handler(static function (Throwable $e): void {
            jsonError('Server error: ' . $e->getMessage(), 500);
        });

        register_shutdown_function(static function (): void {
            $error = error_get_last();
            if ($error === null || !in_array($error['type'], [E_ERROR, E_PARSE, E_CORE_ERROR, E_COMPILE_ERROR], true)) {
                return;
            }
            jsonError('Server error: ' . $error['message'], 500);
        });
    }
}

initApiResponse();

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
    while (ob_get_level() > 0) {
        ob_end_clean();
    }
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
    while (ob_get_level() > 0) {
        ob_end_clean();
    }
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
