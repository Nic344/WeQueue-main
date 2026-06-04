<?php

declare(strict_types=1);

require_once __DIR__ . '/auth.php';

function requireStaffAuth(PDO $pdo): array
{
    $user = requireAuth($pdo);
    $role = $user['role'] ?? 'customer';

    if ($role !== 'staff' && $role !== 'admin') {
        jsonError('Staff or admin access required', 403);
    }

    return $user;
}

function requireAdminAuth(PDO $pdo): array
{
    $user = requireAuth($pdo);
    $role = $user['role'] ?? 'customer';

    if ($role !== 'admin') {
        jsonError('Admin access required', 403);
    }

    return $user;
}

function formatStaffQueueRow(array $row): array
{
    return [
        'id' => (int) $row['id'],
        'queue_number' => $row['queue_number'],
        'customer_name' => $row['customer_name'] ?? null,
        'customer_email' => $row['customer_email'] ?? null,
        'food_id' => isset($row['food_id']) && $row['food_id'] !== null ? (int) $row['food_id'] : null,
        'food_name' => $row['food_name'] ?? null,
        'food_image' => $row['food_image'] ?? null,
        'status' => $row['status'],
        'served_at' => $row['served_at'] ?? null,
        'completed_at' => $row['completed_at'] ?? null,
        'cancelled_at' => $row['cancelled_at'] ?? null,
        'created_at' => $row['created_at'],
        'updated_at' => $row['updated_at'],
    ];
}

function staffQueueSelectSql(string $where = ''): string
{
    return "SELECT q.id, q.queue_number, q.status, q.food_id,
                   q.served_at, q.completed_at, q.cancelled_at, q.created_at, q.updated_at,
                   u.name AS customer_name, u.email AS customer_email,
                   f.name AS food_name, f.image_url AS food_image
            FROM queues q
            INNER JOIN users u ON u.id = q.user_id
            LEFT JOIN foods f ON f.id = q.food_id
            {$where}";
}
