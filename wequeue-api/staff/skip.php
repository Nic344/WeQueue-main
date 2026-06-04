<?php

declare(strict_types=1);

require_once __DIR__ . '/../config/database.php';
require_once __DIR__ . '/../helpers/response.php';
require_once __DIR__ . '/../helpers/staff_auth.php';
require_once __DIR__ . '/../helpers/queue.php';

sendCorsHeaders();
requireMethod('PUT');

$pdo = getDb();
requireStaffAuth($pdo);

$input = getJsonInput();
requireFields($input, ['queue_id']);
$queueId = (int) $input['queue_id'];

try {
    $pdo->beginTransaction();

    $stmt = $pdo->prepare(staffQueueSelectSql("WHERE q.id = :id AND q.status = 'waiting' LIMIT 1 FOR UPDATE"));
    $stmt->execute(['id' => $queueId]);
    $queue = $stmt->fetch();

    if (!$queue) {
        $pdo->rollBack();
        jsonError('Queue not found or not waiting', 404);
    }

    $update = $pdo->prepare('UPDATE queues SET created_at = NOW(), updated_at = NOW() WHERE id = :id');
    $update->execute(['id' => $queueId]);

    $fetch = $pdo->prepare(staffQueueSelectSql('WHERE q.id = :id LIMIT 1'));
    $fetch->execute(['id' => $queueId]);
    $updatedQueue = $fetch->fetch();
    $ticket = formatQueueTicket($pdo, [
        'id' => $updatedQueue['id'],
        'queue_number' => $updatedQueue['queue_number'],
        'status' => $updatedQueue['status'],
        'food_id' => $updatedQueue['food_id'],
        'food_name' => $updatedQueue['food_name'],
        'created_at' => $updatedQueue['created_at'],
        'updated_at' => $updatedQueue['updated_at'],
    ]);

    $pdo->commit();

    $data = formatStaffQueueRow($updatedQueue);
    $data['position'] = $ticket['position'];
    jsonSuccess($data, 'Queue moved to the end');
} catch (PDOException $e) {
    if ($pdo->inTransaction()) {
        $pdo->rollBack();
    }
    jsonError('Unable to skip queue', 500);
}
