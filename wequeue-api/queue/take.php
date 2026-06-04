<?php

declare(strict_types=1);

require_once __DIR__ . '/../config/database.php';
require_once __DIR__ . '/../helpers/response.php';
require_once __DIR__ . '/../helpers/auth.php';
require_once __DIR__ . '/../helpers/queue.php';

sendCorsHeaders();
requireMethod('POST');

$pdo = getDb();
$user = requireAuth($pdo);
$userId = (int) $user['id'];

$input = getJsonInput();
$foodId = isset($input['food_id']) ? (int) $input['food_id'] : null;

if ($foodId !== null && $foodId > 0) {
    $foodCheck = $pdo->prepare('SELECT id FROM foods WHERE id = :id LIMIT 1');
    $foodCheck->execute(['id' => $foodId]);
    if (!$foodCheck->fetch()) {
        jsonError('Food not found', 404);
    }
}

try {
    $pdo->beginTransaction();

    $lockUser = $pdo->prepare('SELECT id FROM users WHERE id = :id FOR UPDATE');
    $lockUser->execute(['id' => $userId]);

    $existing = getActiveQueueForUser($pdo, $userId);
    if ($existing !== null) {
        $ticket = formatQueueTicket($pdo, $existing);
        $pdo->rollBack();
        jsonError('You already have an active queue', 409, $ticket);
    }

    $queueNumber = generateQueueNumber($pdo);

    $stmt = $pdo->prepare(
        'INSERT INTO queues (user_id, queue_number, status, food_id)
         VALUES (:user_id, :queue_number, :status, :food_id)'
    );
    $stmt->execute([
        'user_id' => $userId,
        'queue_number' => $queueNumber,
        'status' => 'waiting',
        'food_id' => $foodId !== null && $foodId > 0 ? $foodId : null,
    ]);

    $queueId = (int) $pdo->lastInsertId();
    $fetch = $pdo->prepare(
        'SELECT q.*, f.name AS food_name FROM queues q
         LEFT JOIN foods f ON f.id = q.food_id WHERE q.id = :id'
    );
    $fetch->execute(['id' => $queueId]);
    $queue = $fetch->fetch();
    $ticket = formatQueueTicket($pdo, $queue);

    $pdo->commit();
    jsonSuccess($ticket, 'Queue ticket created', 201);
} catch (PDOException $e) {
    if ($pdo->inTransaction()) {
        $pdo->rollBack();
    }

    if ($e->getCode() === '23000') {
        jsonError('Queue number already exists. Please try again.', 409);
    }

    jsonError('Unable to create queue ticket', 500);
}
