<?php

declare(strict_types=1);

require_once __DIR__ . '/../config/database.php';
require_once __DIR__ . '/../helpers/response.php';
require_once __DIR__ . '/../helpers/auth.php';

sendCorsHeaders();
requireMethod('GET');

$pdo = getDb();
requireAuth($pdo);

$keyword = trim((string) ($_GET['q'] ?? $_GET['keyword'] ?? ''));

if ($keyword === '') {
    jsonError('Search query parameter "q" is required', 422);
}

$stmt = $pdo->prepare(
    "SELECT id, name, description, price, image_url, category, created_at
     FROM foods
     WHERE name LIKE :q OR description LIKE :q2 OR category LIKE :q3
     ORDER BY name ASC"
);
$like = '%' . $keyword . '%';
$stmt->execute(['q' => $like, 'q2' => $like, 'q3' => $like]);

$foods = array_map(static function (array $row): array {
    $row['id'] = (int) $row['id'];
    $row['price'] = (float) $row['price'];
    return $row;
}, $stmt->fetchAll());

jsonSuccess(['foods' => $foods, 'query' => $keyword, 'count' => count($foods)]);
