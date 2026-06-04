<?php

declare(strict_types=1);

function generateQueueNumber(PDO $pdo): string
{
    $stmt = $pdo->prepare('INSERT INTO queue_counter () VALUES ()');
    $stmt->execute();
    $num = (int) $pdo->lastInsertId();

    return 'A' . str_pad((string) $num, 3, '0', STR_PAD_LEFT);
}

function getServingQueue(PDO $pdo): ?array
{
    $stmt = $pdo->query(
        "SELECT q.*, f.name AS food_name
         FROM queues q
         LEFT JOIN foods f ON f.id = q.food_id
         WHERE q.status = 'serving'
         ORDER BY q.updated_at ASC
         LIMIT 1"
    );
    $row = $stmt->fetch();
    return $row ?: null;
}

function countWaiting(PDO $pdo): int
{
    $stmt = $pdo->query("SELECT COUNT(*) FROM queues WHERE status = 'waiting'");
    return (int) $stmt->fetchColumn();
}

function getQueuePosition(PDO $pdo, array $queue): int
{
    if ($queue['status'] !== 'waiting') {
        return 0;
    }

    $stmt = $pdo->prepare(
        "SELECT COUNT(*) + 1 FROM queues
         WHERE status = 'waiting'
           AND (created_at < :created_at OR (created_at = :created_at2 AND id < :id))"
    );
    $stmt->execute([
        'created_at' => $queue['created_at'],
        'created_at2' => $queue['created_at'],
        'id' => $queue['id'],
    ]);

    return (int) $stmt->fetchColumn();
}

function formatQueueTicket(PDO $pdo, array $queue): array
{
    $position = getQueuePosition($pdo, $queue);
    $waitMinutes = max(0, ($position - 1) * ESTIMATED_WAIT_PER_PERSON);

    return [
        'id' => (int) $queue['id'],
        'queue_number' => $queue['queue_number'],
        'status' => $queue['status'],
        'position' => $position,
        'estimated_wait' => $waitMinutes,
        'estimated_wait_minutes' => $waitMinutes,
        'food_id' => $queue['food_id'] !== null ? (int) $queue['food_id'] : null,
        'food_name' => $queue['food_name'] ?? null,
        'served_at' => $queue['served_at'] ?? null,
        'completed_at' => $queue['completed_at'] ?? null,
        'cancelled_at' => $queue['cancelled_at'] ?? null,
        'created_at' => $queue['created_at'],
        'updated_at' => $queue['updated_at'],
    ];
}

function getActiveQueueForUser(PDO $pdo, int $userId): ?array
{
    $stmt = $pdo->prepare(
        "SELECT q.*, f.name AS food_name
         FROM queues q
         LEFT JOIN foods f ON f.id = q.food_id
         WHERE q.user_id = :user_id AND q.status IN ('waiting', 'serving')
         ORDER BY q.created_at DESC
         LIMIT 1"
    );
    $stmt->execute(['user_id' => $userId]);
    $row = $stmt->fetch();
    return $row ?: null;
}
