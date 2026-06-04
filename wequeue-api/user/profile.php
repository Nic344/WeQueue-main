<?php

declare(strict_types=1);

require_once __DIR__ . '/../config/database.php';
require_once __DIR__ . '/../helpers/response.php';
require_once __DIR__ . '/../helpers/auth.php';

sendCorsHeaders();
requireMethod('GET');

$pdo = getDb();
$user = requireAuth($pdo);

jsonSuccess(['user' => $user]);
