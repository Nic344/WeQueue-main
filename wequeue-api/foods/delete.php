<?php

declare(strict_types=1);

require_once __DIR__ . '/../config/database.php';
require_once __DIR__ . '/../helpers/response.php';
require_once __DIR__ . '/../helpers/staff_auth.php';

sendCorsHeaders();
requireMethod('DELETE');

$pdo = getDb();
requireAdminAuth($pdo);

$input = getJsonInput();
$id = isset($input['id'])
    ? (int) $input['id']
    : (isset($input['food_id']) ? (int) $input['food_id'] : (int) ($_GET['id'] ?? 0));

if ($id <= 0) {
    jsonError('Food id is required', 422);
}

$check = $pdo->prepare('SELECT id, name FROM foods WHERE id = :id LIMIT 1');
$check->execute(['id' => $id]);
$food = $check->fetch();

if (!$food) {
    jsonError('Food not found', 404);
}

$stmt = $pdo->prepare('DELETE FROM foods WHERE id = :id');
$stmt->execute(['id' => $id]);

jsonSuccess(['id' => $id, 'name' => $food['name']], 'Food deleted');
