<?php

declare(strict_types=1);

require_once __DIR__ . '/../config/database.php';
require_once __DIR__ . '/../helpers/response.php';
require_once __DIR__ . '/../helpers/auth.php';
require_once __DIR__ . '/../helpers/queue.php';

sendCorsHeaders();
requireMethod('GET');

$pdo = getDb();
$user = requireAuth($pdo);

$serving = getServingQueue($pdo);
$totalWaiting = countWaiting($pdo);

$yourQueue = null;
$remaining = $totalWaiting;

$active = getActiveQueueForUser($pdo, (int) $user['id']);
if ($active !== null) {
    $ticket = formatQueueTicket($pdo, $active);
    $yourQueue = $ticket['queue_number'];
    if ($active['status'] === 'waiting') {
        $remaining = max(0, $ticket['position'] - 1);
    } else {
        $remaining = 0;
    }
}

jsonSuccess([
    'now_serving' => $serving ? $serving['queue_number'] : null,
    'now_serving_queue' => $serving ? formatQueueTicket($pdo, $serving) : null,
    'total_waiting' => $totalWaiting,
    'remaining' => $remaining,
    'your_queue' => $yourQueue,
    'estimated_wait_per_person' => ESTIMATED_WAIT_PER_PERSON,
    'estimated_wait_minutes' => $remaining * ESTIMATED_WAIT_PER_PERSON,
]);
