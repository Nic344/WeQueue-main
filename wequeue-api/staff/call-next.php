<?php

declare(strict_types=1);

require_once __DIR__ . '/../config/database.php';
require_once __DIR__ . '/../helpers/response.php';
require_once __DIR__ . '/../helpers/staff_auth.php';

sendCorsHeaders();
requireMethod('POST');

$pdo = getDb();
requireStaffAuth($pdo);

try {
    $pdo->beginTransaction();

    $servingStmt = $pdo->query(
        staffQueueSelectSql("WHERE q.status = 'serving' ORDER BY q.updated_at ASC LIMIT 1 FOR UPDATE")
    );
    $serving = $servingStmt->fetch();
    if ($serving) {
        $pdo->rollBack();
        jsonError('Please complete current queue first', 409, formatStaffQueueRow($serving));
    }

    $waitingStmt = $pdo->query(
        staffQueueSelectSql("WHERE q.status = 'waiting' ORDER BY q.created_at ASC, q.id ASC LIMIT 1 FOR UPDATE")
    );
    $waiting = $waitingStmt->fetch();
    if (!$waiting) {
        $pdo->commit();
        jsonSuccess(null, 'No waiting queues');
    }

    $update = $pdo->prepare(
        "UPDATE queues SET status = 'serving', served_at = NOW(), updated_at = NOW() WHERE id = :id"
    );
    $update->execute(['id' => $waiting['id']]);

    $fetch = $pdo->prepare(staffQueueSelectSql('WHERE q.id = :id LIMIT 1'));
    $fetch->execute(['id' => $waiting['id']]);
    $queue = $fetch->fetch();

    $pdo->commit();
    jsonSuccess(formatStaffQueueRow($queue), 'Queue is now serving');
} catch (PDOException $e) {
    if ($pdo->inTransaction()) {
        $pdo->rollBack();
    }
    jsonError('Unable to call next queue', 500);
}
