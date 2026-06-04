<?php

declare(strict_types=1);

require_once __DIR__ . '/../config/database.php';
require_once __DIR__ . '/../helpers/response.php';
require_once __DIR__ . '/../helpers/auth.php';
require_once __DIR__ . '/../helpers/queue.php';

sendCorsHeaders();
requireMethod('PUT');

$pdo = getDb();
$user = requireAuth($pdo);

$active = getActiveQueueForUser($pdo, (int) $user['id']);

if ($active === null) {
    jsonError('No active queue to cancel', 404);
}

if ($active['status'] === 'serving') {
    jsonError('Cannot cancel a queue that is currently being served', 409);
}

$stmt = $pdo->prepare(
    "UPDATE queues SET status = 'cancelled', cancelled_at = NOW(), updated_at = NOW()
     WHERE id = :id AND user_id = :user_id"
);
$stmt->execute(['id' => $active['id'], 'user_id' => $user['id']]);

$fetch = $pdo->prepare(
    'SELECT q.*, f.name AS food_name FROM queues q
     LEFT JOIN foods f ON f.id = q.food_id WHERE q.id = :id'
);
$fetch->execute(['id' => $active['id']]);
$queue = $fetch->fetch();

jsonSuccess(formatQueueTicket($pdo, $queue), 'Queue cancelled successfully');
