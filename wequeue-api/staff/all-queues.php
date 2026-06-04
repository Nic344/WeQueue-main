<?php

declare(strict_types=1);

require_once __DIR__ . '/../config/database.php';
require_once __DIR__ . '/../helpers/response.php';
require_once __DIR__ . '/../helpers/staff_auth.php';

sendCorsHeaders();
requireMethod('GET');

$pdo = getDb();
requireStaffAuth($pdo);

$allowedStatuses = ['waiting', 'serving', 'completed', 'cancelled', 'all'];
$status = strtolower(trim((string) ($_GET['status'] ?? 'all')));
if (!in_array($status, $allowedStatuses, true)) {
    jsonError('Invalid status filter', 422);
}

$date = trim((string) ($_GET['date'] ?? ''));
if ($date !== '' && !preg_match('/^\d{4}-\d{2}-\d{2}$/', $date)) {
    jsonError('Invalid date format. Use YYYY-MM-DD', 422);
}

$page = max(1, (int) ($_GET['page'] ?? 1));
$limit = min(100, max(1, (int) ($_GET['limit'] ?? 20)));
$offset = ($page - 1) * $limit;

$where = [];
$params = [];

if ($status !== 'all') {
    $where[] = 'q.status = :status';
    $params['status'] = $status;
}

if ($date !== '') {
    $where[] = 'DATE(q.created_at) = :date';
    $params['date'] = $date;
}

$whereSql = $where !== [] ? 'WHERE ' . implode(' AND ', $where) : '';

$countStmt = $pdo->prepare("SELECT COUNT(*) FROM queues q {$whereSql}");
$countStmt->execute($params);
$total = (int) $countStmt->fetchColumn();

$sql = staffQueueSelectSql($whereSql . ' ORDER BY q.created_at DESC, q.id DESC LIMIT :limit OFFSET :offset');
$stmt = $pdo->prepare($sql);
foreach ($params as $key => $value) {
    $stmt->bindValue(':' . $key, $value);
}
$stmt->bindValue(':limit', $limit, PDO::PARAM_INT);
$stmt->bindValue(':offset', $offset, PDO::PARAM_INT);
$stmt->execute();

$queues = array_map('formatStaffQueueRow', $stmt->fetchAll());

jsonSuccess([
    'queues' => $queues,
    'pagination' => [
        'page' => $page,
        'limit' => $limit,
        'total' => $total,
        'total_pages' => (int) ceil($total / $limit),
    ],
]);
