<?php

declare(strict_types=1);

require_once __DIR__ . '/../config/database.php';
require_once __DIR__ . '/../helpers/response.php';
require_once __DIR__ . '/../helpers/auth.php';

sendCorsHeaders();
requireMethod('GET');

$pdo = getDb();
requireAuth($pdo);

$stmt = $pdo->query(
    "SELECT f.id, f.name, f.description, f.price, f.image_url, f.category, f.created_at,
            COUNT(q.id) AS queue_count
     FROM foods f
     LEFT JOIN queues q ON q.food_id = f.id
     GROUP BY f.id
     ORDER BY queue_count DESC, f.name ASC
     LIMIT 6"
);

$foods = array_map(static function (array $row): array {
    return [
        'id' => (int) $row['id'],
        'name' => $row['name'],
        'description' => $row['description'],
        'price' => (float) $row['price'],
        'image_url' => $row['image_url'],
        'category' => $row['category'],
        'queue_count' => (int) $row['queue_count'],
        'created_at' => $row['created_at'],
    ];
}, $stmt->fetchAll());

jsonSuccess(['foods' => $foods]);
