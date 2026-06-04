<?php

declare(strict_types=1);

require_once __DIR__ . '/../../config/database.php';
require_once __DIR__ . '/../../helpers/response.php';
require_once __DIR__ . '/../../helpers/staff_auth.php';

sendCorsHeaders();
requireMethod('PUT');

$pdo = getDb();
requireAdminAuth($pdo);

$input = getJsonInput();
requireFields($input, ['user_id', 'role']);

$userId = (int) $input['user_id'];
$role = strtolower(trim((string) $input['role']));
$allowedRoles = ['customer', 'staff', 'admin'];

if ($userId <= 0) {
    jsonError('Valid user_id is required', 422);
}

if (!in_array($role, $allowedRoles, true)) {
    jsonError('Invalid role', 422);
}

$check = $pdo->prepare('SELECT id FROM users WHERE id = :id LIMIT 1');
$check->execute(['id' => $userId]);
if (!$check->fetch()) {
    jsonError('User not found', 404);
}

$update = $pdo->prepare('UPDATE users SET role = :role WHERE id = :id');
$update->execute([
    'role' => $role,
    'id' => $userId,
]);

$fetch = $pdo->prepare(
    'SELECT id, name, email, role, profile_picture, created_at
     FROM users WHERE id = :id LIMIT 1'
);
$fetch->execute(['id' => $userId]);
$user = $fetch->fetch();
$user['id'] = (int) $user['id'];

jsonSuccess($user, 'User role updated');
