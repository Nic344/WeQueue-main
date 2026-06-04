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
    "SELECT q.id, q.queue_number, q.status, q.created_at, q.updated_at,
            f.id AS food_id, f.name AS food_name, f.price AS food_price, f.image_url AS food_image
     FROM queues q
     LEFT JOIN foods f ON f.id = q.food_id
     WHERE q.user_id = :user_id
       AND q.status IN ('completed', 'cancelled', 'serving')
     ORDER BY q.created_at DESC"
);
$stmt->execute(['user_id' => $user['id']]);
$rows = $stmt->fetchAll();

$history = array_map(static function (array $row): array {
    return [
        'id' => (int) $row['id'],
        'queue_number' => $row['queue_number'],
        'status' => $row['status'],
        'status_label' => ucfirst($row['status']),
        'food_id' => $row['food_id'] !== null ? (int) $row['food_id'] : null,
        'food_name' => $row['food_name'],
        'food_price' => $row['food_price'] !== null ? (float) $row['food_price'] : null,
        'food_image' => $row['food_image'],
        'date' => $row['created_at'],
        'created_at' => $row['created_at'],
        'updated_at' => $row['updated_at'],
    ];
}, $rows);

jsonSuccess(['history' => $history, 'count' => count($history)]);
