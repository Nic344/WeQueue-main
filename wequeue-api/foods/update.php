<?php

declare(strict_types=1);

require_once __DIR__ . '/../config/database.php';
require_once __DIR__ . '/../helpers/response.php';
require_once __DIR__ . '/../helpers/staff_auth.php';
require_once __DIR__ . '/../helpers/uploads.php';

sendCorsHeaders();
requireMethod('PUT');

$pdo = getDb();
requireAdminAuth($pdo);

$input = getJsonInput();
requireFields($input, ['id']);

$id = (int) $input['id'];

$check = $pdo->prepare('SELECT * FROM foods WHERE id = :id LIMIT 1');
$check->execute(['id' => $id]);
$existing = $check->fetch();

if (!$existing) {
    jsonError('Food not found', 404);
}

$name = isset($input['name']) ? trim((string) $input['name']) : $existing['name'];
$description = array_key_exists('description', $input)
    ? trim((string) $input['description'])
    : $existing['description'];
$price = isset($input['price']) ? (float) $input['price'] : (float) $existing['price'];
$imageUrl = array_key_exists('image_url', $input)
    ? trim((string) $input['image_url'])
    : $existing['image_url'];
$category = isset($input['category']) ? trim((string) $input['category']) : $existing['category'];
$isAvailable = array_key_exists('is_available', $input)
    ? (int) (bool) $input['is_available']
    : (int) $existing['is_available'];

if ($price < 0) {
    jsonError('Price must be zero or greater', 422);
}

$stmt = $pdo->prepare(
    'UPDATE foods SET name = :name, description = :description, price = :price,
     image_url = :image_url, category = :category, is_available = :is_available WHERE id = :id'
);
$stmt->execute([
    'id' => $id,
    'name' => $name,
    'description' => $description,
    'price' => $price,
    'image_url' => $imageUrl !== '' ? $imageUrl : null,
    'category' => $category,
    'is_available' => $isAvailable,
]);

$oldImage = $existing['image_url'] ?? null;
$newImage = $imageUrl !== '' ? $imageUrl : null;
if ($oldImage !== null && $oldImage !== $newImage) {
    deleteUploadedImage($oldImage);
}

$fetch = $pdo->prepare('SELECT * FROM foods WHERE id = :id');
$fetch->execute(['id' => $id]);
$food = $fetch->fetch();
$food['id'] = (int) $food['id'];
$food['price'] = (float) $food['price'];
$food['is_available'] = (bool) $food['is_available'];

jsonSuccess($food, 'Food updated');
