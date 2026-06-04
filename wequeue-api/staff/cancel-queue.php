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
requireFields($input, ['queue_id']);
$queueId = (int) $input['queue_id'];
$reason = isset($input['reason']) ? trim((string) $input['reason']) : null;

try {
    $pdo->beginTransaction();

    $stmt = $pdo->prepare(staffQueueSelectSql('WHERE q.id = :id LIMIT 1 FOR UPDATE'));
    $stmt->execute(['id' => $queueId]);
    $queue = $stmt->fetch();

    if (!$queue) {
        $pdo->rollBack();
        jsonError('Queue not found', 404);
    }

    if ($queue['status'] !== 'waiting' && $queue['status'] !== 'serving') {
        $pdo->rollBack();
        jsonError('Queue is already completed or cancelled', 409);
    }

    $update = $pdo->prepare(
        "UPDATE queues SET status = 'cancelled', cancelled_at = NOW(), updated_at = NOW() WHERE id = :id"
    );
    $update->execute(['id' => $queueId]);

    $fetch = $pdo->prepare(staffQueueSelectSql('WHERE q.id = :id LIMIT 1'));
    $fetch->execute(['id' => $queueId]);
    $updatedQueue = $fetch->fetch();

    $pdo->commit();

    $data = formatStaffQueueRow($updatedQueue);
    if ($reason !== null && $reason !== '') {
        $data['reason'] = $reason;
    }
    jsonSuccess($data, 'Queue cancelled');
} catch (PDOException $e) {
    if ($pdo->inTransaction()) {
        $pdo->rollBack();
    }
    jsonError('Unable to cancel queue', 500);
}
