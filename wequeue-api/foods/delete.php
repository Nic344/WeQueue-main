<?php

declare(strict_types=1);

require_once __DIR__ . '/../config/database.php';
require_once __DIR__ . '/../helpers/response.php';
require_once __DIR__ . '/../helpers/staff_auth.php';
require_once __DIR__ . '/../helpers/uploads.php';

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

$check = $pdo->prepare('SELECT id, name, image_url FROM foods WHERE id = :id LIMIT 1');
$check->execute(['id' => $id]);
$food = $check->fetch();

if (!$food) {
    jsonError('Food not found', 404);
}

$stmt = $pdo->prepare('DELETE FROM foods WHERE id = :id');
$stmt->execute(['id' => $id]);

// Remove the associated uploaded image file from the server (if any).
deleteUploadedImage($food['image_url'] ?? null);

jsonSuccess(['id' => $id, 'name' => $food['name']], 'Food deleted');

