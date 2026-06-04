<?php

declare(strict_types=1);

require_once __DIR__ . '/../config/database.php';
require_once __DIR__ . '/../helpers/response.php';
require_once __DIR__ . '/../helpers/auth.php';

sendCorsHeaders();
requireMethod('GET');

$pdo = getDb();
$user = requireAuth($pdo);

$stmt = $pdo->prepare(
    "SELECT f.id, f.name, f.description, f.price, f.image_url, f.category, f.created_at,
            fav.id AS favorite_id, fav.created_at AS favorited_at
     FROM favorites fav
     INNER JOIN foods f ON f.id = fav.food_id
     WHERE fav.user_id = :user_id
     ORDER BY fav.created_at DESC"
);
$stmt->execute(['user_id' => $user['id']]);

$foods = array_map(static function (array $row): array {
    return [
        'favorite_id' => (int) $row['favorite_id'],
        'id' => (int) $row['id'],
        'name' => $row['name'],
        'description' => $row['description'],
        'price' => (float) $row['price'],
        'image_url' => $row['image_url'],
        'category' => $row['category'],
        'favorited_at' => $row['favorited_at'],
        'created_at' => $row['created_at'],
    ];
}, $stmt->fetchAll());

jsonSuccess(['favorites' => $foods, 'count' => count($foods)]);
