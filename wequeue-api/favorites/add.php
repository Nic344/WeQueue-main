<?php

declare(strict_types=1);

require_once __DIR__ . '/../config/database.php';
require_once __DIR__ . '/../helpers/response.php';
require_once __DIR__ . '/../helpers/auth.php';

sendCorsHeaders();
requireMethod('POST');

$pdo = getDb();
$user = requireAuth($pdo);

$input = getJsonInput();
requireFields($input, ['food_id']);

$foodId = (int) $input['food_id'];

$foodCheck = $pdo->prepare('SELECT id, name FROM foods WHERE id = :id LIMIT 1');
$foodCheck->execute(['id' => $foodId]);
$food = $foodCheck->fetch();

if (!$food) {
    jsonError('Food not found', 404);
}

$dup = $pdo->prepare('SELECT id FROM favorites WHERE user_id = :user_id AND food_id = :food_id');
$dup->execute(['user_id' => $user['id'], 'food_id' => $foodId]);
if ($dup->fetch()) {
    jsonError('Food is already in favorites', 409);
}

$stmt = $pdo->prepare('INSERT INTO favorites (user_id, food_id) VALUES (:user_id, :food_id)');
$stmt->execute(['user_id' => $user['id'], 'food_id' => $foodId]);

jsonSuccess([
    'favorite_id' => (int) $pdo->lastInsertId(),
    'food_id' => $foodId,
    'food_name' => $food['name'],
], 'Added to favorites', 201);
