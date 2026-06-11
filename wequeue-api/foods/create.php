<?php

declare(strict_types=1);

require_once __DIR__ . '/../config/database.php';
require_once __DIR__ . '/../helpers/response.php';
require_once __DIR__ . '/../helpers/staff_auth.php';

sendCorsHeaders();
requireMethod('POST');

$pdo = getDb();
requireAdminAuth($pdo);

$input = getJsonInput();
requireFields($input, ['name', 'price']);

$name = trim((string) $input['name']);
$price = (float) $input['price'];
$description = trim((string) ($input['description'] ?? ''));
$imageUrl = trim((string) ($input['image_url'] ?? ''));
$category = trim((string) ($input['category'] ?? 'General'));
$isAvailable = array_key_exists('is_available', $input) ? (int) (bool) $input['is_available'] : 1;

if ($price < 0) {
    jsonError('Price must be zero or greater', 422);
}

$stmt = $pdo->prepare(
    'INSERT INTO foods (name, description, price, image_url, category, is_available)
     VALUES (:name, :description, :price, :image_url, :category, :is_available)'
);
$stmt->execute([
    'name' => $name,
    'description' => $description !== '' ? $description : null,
    'price' => $price,
    'image_url' => $imageUrl !== '' ? $imageUrl : null,
    'category' => $category !== '' ? $category : 'General',
    'is_available' => $isAvailable,
]);

$id = (int) $pdo->lastInsertId();
$fetch = $pdo->prepare('SELECT * FROM foods WHERE id = :id');
$fetch->execute(['id' => $id]);
$food = $fetch->fetch();
$food['id'] = (int) $food['id'];
$food['price'] = (float) $food['price'];
$food['is_available'] = (bool) $food['is_available'];

jsonSuccess($food, 'Food created', 201);
