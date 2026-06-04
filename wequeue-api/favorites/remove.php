<?php

declare(strict_types=1);

require_once __DIR__ . '/../config/database.php';
require_once __DIR__ . '/../helpers/response.php';
require_once __DIR__ . '/../helpers/auth.php';

sendCorsHeaders();
requireMethod('DELETE');

$pdo = getDb();
$user = requireAuth($pdo);

$input = getJsonInput();
$foodId = isset($input['food_id']) ? (int) $input['food_id'] : (int) ($_GET['food_id'] ?? 0);

if ($foodId <= 0) {
    jsonError('food_id is required', 422);
}

$stmt = $pdo->prepare(
    'DELETE FROM favorites WHERE user_id = :user_id AND food_id = :food_id'
);
$stmt->execute(['user_id' => $user['id'], 'food_id' => $foodId]);

if ($stmt->rowCount() === 0) {
    jsonError('Favorite not found', 404);
}

jsonSuccess(['food_id' => $foodId], 'Removed from favorites');
