<?php

declare(strict_types=1);

/**
 * PDO database connection — adjust credentials for your environment.
 */
define('DB_HOST', getenv('DB_HOST') ?: 'pq7vi9.h.filess.io:61032');
define('DB_NAME', getenv('DB_NAME') ?: 'mobiledb_possibleus');
define('DB_USER', getenv('DB_USER') ?: 'mobiledb_possibleus');
define('DB_PASS', getenv('DB_PASS') ?: 'cc31f5b40ea67e6e9dea000b4fe168f3cac9a59b');
define('DB_CHARSET', 'utf8mb4');

/** Minutes estimated per person in queue */
define('ESTIMATED_WAIT_PER_PERSON', 3);

function getDb(): PDO
{
    static $pdo = null;

    if ($pdo instanceof PDO) {
        return $pdo;
    }

    $dsn = sprintf('mysql:host=%s;dbname=%s;charset=%s', DB_HOST, DB_NAME, DB_CHARSET);

    $pdo = new PDO($dsn, DB_USER, DB_PASS, [
        PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
        PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
        PDO::ATTR_EMULATE_PREPARES => false,
    ]);

    return $pdo;
}
