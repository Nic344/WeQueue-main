<?php

declare(strict_types=1);

require_once __DIR__ . '/../config/database.php';
require_once __DIR__ . '/../helpers/response.php';

sendCorsHeaders();
requireMethod('GET');

$pdo = getDb();

$stmt = $pdo->query(
    'SELECT id, name, description, price, image_url, category, is_available, created_at
     FROM foods ORDER BY name ASC'
);

$foods = array_map(static function (array $row): array {
    $row['id'] = (int) $row['id'];
    $row['price'] = (float) $row['price'];
    $row['is_available'] = (bool) $row['is_available'];
    return $row;
}, $stmt->fetchAll());

jsonSuccess(['foods' => $foods, 'count' => count($foods)]);
