<?php

declare(strict_types=1);

require_once __DIR__ . '/../config/database.php';
require_once __DIR__ . '/../helpers/response.php';
require_once __DIR__ . '/../helpers/auth.php';

sendCorsHeaders();
requireMethod('POST');

$token = getBearerToken();
if ($token === null) {
    jsonError('Authorization token required', 401);
}

$pdo = getDb();
if (!revokeToken($pdo, $token)) {
    jsonError('Invalid or already revoked token', 401);
}

jsonSuccess(null, 'Logged out successfully');
