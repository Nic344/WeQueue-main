<?php

declare(strict_types=1);

require_once __DIR__ . '/../config/database.php';
require_once __DIR__ . '/../helpers/response.php';
require_once __DIR__ . '/../helpers/staff_auth.php';

sendCorsHeaders();
requireMethod('GET');

$pdo = getDb();
requireAdminAuth($pdo);

$stmt = $pdo->query(
    'SELECT id, name, email, role, profile_picture, created_at
     FROM users
     ORDER BY created_at DESC, id DESC'
);

$users = array_map(static function (array $row): array {
    $row['id'] = (int) $row['id'];
    return $row;
}, $stmt->fetchAll());

jsonSuccess([
    'users' => $users,
    'count' => count($users),
]);
