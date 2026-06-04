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

if ($input === []) {
    jsonError('No fields to update', 422);
}

$name = isset($input['name']) ? trim((string) $input['name']) : $user['name'];
$email = isset($input['email']) ? strtolower(trim((string) $input['email'])) : $user['email'];
$profilePicture = array_key_exists('profile_picture', $input)
    ? trim((string) $input['profile_picture'])
    : $user['profile_picture'];

if ($name === '') {
    jsonError('Name cannot be empty', 422);
}

if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
    jsonError('Invalid email address', 422);
}

$dup = $pdo->prepare('SELECT id FROM users WHERE email = :email AND id != :id LIMIT 1');
$dup->execute(['email' => $email, 'id' => $userId]);
if ($dup->fetch()) {
    jsonError('Email already in use', 409);
}

$stmt = $pdo->prepare(
    'UPDATE users SET name = :name, email = :email, profile_picture = :profile_picture WHERE id = :id'
);
$stmt->execute([
    'id' => $userId,
    'name' => $name,
    'email' => $email,
    'profile_picture' => $profilePicture !== '' ? $profilePicture : null,
]);

$fetch = $pdo->prepare('SELECT id, name, email, role, profile_picture, created_at FROM users WHERE id = :id');
$fetch->execute(['id' => $userId]);
$updated = $fetch->fetch();

jsonSuccess(['user' => $updated], 'Profile updated');
