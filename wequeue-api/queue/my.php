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

$active = getActiveQueueForUser($pdo, (int) $user['id']);

if ($active === null) {
    jsonSuccess([
        'has_active_queue' => false,
        'queue' => null,
    ], 'No active queue');
}

jsonSuccess([
    'has_active_queue' => true,
    'queue' => formatQueueTicket($pdo, $active),
]);
