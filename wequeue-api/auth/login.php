<?php

declare(strict_types=1);

require_once __DIR__ . '/../config/database.php';
require_once __DIR__ . '/../helpers/response.php';
require_once __DIR__ . '/../helpers/auth.php';

sendCorsHeaders();
requireMethod('POST');

$input = getJsonInput();
requireFields($input, ['email', 'password']);

$email = strtolower(trim((string) $input['email']));
$password = (string) $input['password'];

$pdo = getDb();
$stmt = $pdo->prepare('SELECT * FROM users WHERE email = :email LIMIT 1');
$stmt->execute(['email' => $email]);
$user = $stmt->fetch();

if (!$user || !password_verify($password, $user['password'])) {
    jsonError('Invalid email or password', 401);
}

$token = createToken($pdo, (int) $user['id']);

jsonSuccess([
    'token' => $token,
    'user' => sanitizeUser($user),
], 'Login successful');
