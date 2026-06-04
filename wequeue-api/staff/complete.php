<?php

declare(strict_types=1);

require_once __DIR__ . '/../config/database.php';
require_once __DIR__ . '/../helpers/response.php';
require_once __DIR__ . '/../helpers/staff_auth.php';

sendCorsHeaders();
requireMethod('PUT');

$pdo = getDb();
requireStaffAuth($pdo);

$input = getJsonInput();
$queueId = isset($input['queue_id']) ? (int) $input['queue_id'] : 0;

try {
    $pdo->beginTransaction();

    if ($queueId > 0) {
        $stmt = $pdo->prepare(staffQueueSelectSql("WHERE q.id = :id AND q.status = 'serving' LIMIT 1 FOR UPDATE"));
        $stmt->execute(['id' => $queueId]);
    } else {
        $stmt = $pdo->query(staffQueueSelectSql("WHERE q.status = 'serving' ORDER BY q.updated_at ASC LIMIT 1 FOR UPDATE"));
    }

    $queue = $stmt->fetch();
    if (!$queue) {
        $pdo->rollBack();
        jsonError('No serving queue found', 404);
    }

    $update = $pdo->prepare(
        "UPDATE queues SET status = 'completed', completed_at = NOW(), updated_at = NOW() WHERE id = :id"
    );
    $update->execute(['id' => $queue['id']]);

    $fetch = $pdo->prepare(staffQueueSelectSql('WHERE q.id = :id LIMIT 1'));
    $fetch->execute(['id' => $queue['id']]);
    $updatedQueue = $fetch->fetch();

    $pdo->commit();
    jsonSuccess(formatStaffQueueRow($updatedQueue), 'Queue completed');
} catch (PDOException $e) {
    if ($pdo->inTransaction()) {
        $pdo->rollBack();
    }
    jsonError('Unable to complete queue', 500);
}
