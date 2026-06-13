<?php

declare(strict_types=1);

require_once __DIR__ . '/../config/database.php';
require_once __DIR__ . '/../helpers/response.php';
require_once __DIR__ . '/../helpers/auth.php';

sendCorsHeaders();
requireMethod('PUT');

$pdo = getDb();
$user = requireAuth($pdo);
$userId = (int) $user['id'];

$input = getJsonInput();
requireFields($input, ['current_password', 'new_password']);

$currentPassword = (string) $input['current_password'];
$newPassword = (string) $input['new_password'];

if (strlen($newPassword) < 6) {
    jsonError('New password must be at least 6 characters', 422);
}

if ($newPassword === $currentPassword) {
    jsonError('New password must be different from the current one', 422);
}

$stmt = $pdo->prepare('SELECT password FROM users WHERE id = :id LIMIT 1');
$stmt->execute(['id' => $userId]);
$row = $stmt->fetch();

if (!$row || !password_verify($currentPassword, $row['password'])) {
    jsonError('Current password is incorrect', 401);
}

$newHash = password_hash($newPassword, PASSWORD_DEFAULT);

$update = $pdo->prepare('UPDATE users SET password = :password WHERE id = :id');
$update->execute([
    'password' => $newHash,
    'id' => $userId,
]);

$currentToken = getBearerToken();
if ($currentToken !== null) {
    $revoke = $pdo->prepare('DELETE FROM tokens WHERE user_id = :id AND token != :token');
    $revoke->execute(['id' => $userId, 'token' => $currentToken]);
}

jsonSuccess(null, 'Password changed successfully');
