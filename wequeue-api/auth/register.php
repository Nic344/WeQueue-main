<?php

declare(strict_types=1);

require_once __DIR__ . '/../config/database.php';
require_once __DIR__ . '/../helpers/response.php';
require_once __DIR__ . '/../helpers/auth.php';

sendCorsHeaders();
requireMethod('POST');

$input = getJsonInput();
requireFields($input, ['name', 'email', 'password']);

$name = trim((string) $input['name']);
$email = strtolower(trim((string) $input['email']));
$password = (string) $input['password'];

if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
    jsonError('Invalid email address', 422);
}

if (strlen($password) < 6) {
    jsonError('Password must be at least 6 characters', 422);
}

$pdo = getDb();

$check = $pdo->prepare('SELECT id FROM users WHERE email = :email LIMIT 1');
$check->execute(['email' => $email]);
if ($check->fetch()) {
    jsonError('Email already registered', 409);
}

$hash = password_hash($password, PASSWORD_DEFAULT);
$stmt = $pdo->prepare(
    'INSERT INTO users (name, email, password) VALUES (:name, :email, :password)'
);
$stmt->execute(['name' => $name, 'email' => $email, 'password' => $hash]);

$userId = (int) $pdo->lastInsertId();
$token = createToken($pdo, $userId);

$userStmt = $pdo->prepare('SELECT id, name, email, role, profile_picture, created_at FROM users WHERE id = :id');
$userStmt->execute(['id' => $userId]);
$user = $userStmt->fetch();

jsonSuccess([
    'token' => $token,
    'user' => $user,
], 'Registration successful', 201);
