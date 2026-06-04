<?php

declare(strict_types=1);

require_once __DIR__ . '/../config/database.php';
require_once __DIR__ . '/../helpers/response.php';
require_once __DIR__ . '/../helpers/staff_auth.php';

sendCorsHeaders();
requireMethod('GET');

$pdo = getDb();
requireStaffAuth($pdo);

$servingStmt = $pdo->query(staffQueueSelectSql("WHERE q.status = 'serving' ORDER BY q.updated_at ASC LIMIT 1"));
$serving = $servingStmt->fetch();

$waitingCount = (int) $pdo->query("SELECT COUNT(*) FROM queues WHERE status = 'waiting'")->fetchColumn();

$completedStmt = $pdo->query(
    "SELECT COUNT(*) FROM queues
     WHERE status = 'completed' AND DATE(updated_at) = CURDATE()"
);
$completedToday = (int) $completedStmt->fetchColumn();

$cancelledStmt = $pdo->query(
    "SELECT COUNT(*) FROM queues
     WHERE status = 'cancelled' AND DATE(updated_at) = CURDATE()"
);
$cancelledToday = (int) $cancelledStmt->fetchColumn();

$waitingStmt = $pdo->query(
    staffQueueSelectSql("WHERE q.status = 'waiting' ORDER BY q.created_at ASC LIMIT 5")
);
$waitingList = array_map(static function (array $row): array {
    $queue = formatStaffQueueRow($row);
    return [
        'id' => $queue['id'],
        'queue_number' => $queue['queue_number'],
        'customer_name' => $queue['customer_name'],
        'food_name' => $queue['food_name'],
        'wait_since' => $queue['created_at'],
        'created_at' => $queue['created_at'],
    ];
}, $waitingStmt->fetchAll());

jsonSuccess([
    'now_serving' => $serving ? formatStaffQueueRow($serving) : null,
    'waiting_count' => $waitingCount,
    'completed_today' => $completedToday,
    'cancelled_today' => $cancelledToday,
    'waiting_list' => $waitingList,
]);
