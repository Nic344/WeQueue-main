<?php

declare(strict_types=1);

require_once __DIR__ . '/../config/database.php';
require_once __DIR__ . '/../helpers/response.php';
require_once __DIR__ . '/../helpers/staff_auth.php';

sendCorsHeaders();
requireMethod('GET');

$pdo = getDb();
requireStaffAuth($pdo);

$totalStmt = $pdo->query("SELECT COUNT(*) FROM queues WHERE DATE(created_at) = CURDATE()");
$totalQueues = (int) $totalStmt->fetchColumn();

$completedStmt = $pdo->query(
    "SELECT COUNT(*) FROM queues WHERE status = 'completed' AND DATE(completed_at) = CURDATE()"
);
$completedCount = (int) $completedStmt->fetchColumn();

$cancelledStmt = $pdo->query(
    "SELECT COUNT(*) FROM queues WHERE status = 'cancelled' AND DATE(cancelled_at) = CURDATE()"
);
$cancelledCount = (int) $cancelledStmt->fetchColumn();

$servingStmt = $pdo->query(staffQueueSelectSql("WHERE q.status = 'serving' ORDER BY q.updated_at ASC LIMIT 1"));
$serving = $servingStmt->fetch();

$averageStmt = $pdo->query(
    "SELECT AVG(TIMESTAMPDIFF(MINUTE, served_at, completed_at)) AS average_minutes
     FROM queues
     WHERE status = 'completed'
       AND served_at IS NOT NULL
       AND completed_at IS NOT NULL
       AND DATE(completed_at) = CURDATE()"
);
$averageServeTime = $averageStmt->fetchColumn();

$peakStmt = $pdo->query(
    "SELECT HOUR(created_at) AS queue_hour, COUNT(*) AS total
     FROM queues
     WHERE DATE(created_at) = CURDATE()
     GROUP BY HOUR(created_at)
     ORDER BY total DESC, queue_hour ASC
     LIMIT 1"
);
$peak = $peakStmt->fetch();

jsonSuccess([
    'total_queues' => $totalQueues,
    'completed_count' => $completedCount,
    'cancelled_count' => $cancelledCount,
    'currently_serving' => $serving ? formatStaffQueueRow($serving) : null,
    'average_serve_time' => $averageServeTime !== null ? round((float) $averageServeTime, 2) : 0,
    'peak_hour' => $peak ? sprintf('%02d:00', (int) $peak['queue_hour']) : null,
]);
