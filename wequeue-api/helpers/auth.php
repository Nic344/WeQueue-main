<?php

declare(strict_types=1);

require_once __DIR__ . '/response.php';

function getBearerToken(): ?string
{
    $header = null;

    if (isset($_SERVER['HTTP_AUTHORIZATION'])) {
        $header = $_SERVER['HTTP_AUTHORIZATION'];
    } elseif (isset($_SERVER['REDIRECT_HTTP_AUTHORIZATION'])) {
        $header = $_SERVER['REDIRECT_HTTP_AUTHORIZATION'];
    } elseif (function_exists('getallheaders')) {
        $headers = getallheaders();
        foreach ($headers as $key => $value) {
            if (strcasecmp($key, 'Authorization') === 0) {
                $header = $value;
                break;
            }
        }
    }

    if ($header === null || !preg_match('/Bearer\s+(\S+)/i', $header, $matches)) {
        return null;
    }

    return $matches[1];
}

function validateToken(PDO $pdo, string $token): ?array
{
    $stmt = $pdo->prepare(
        'SELECT u.id, u.name, u.email, u.role, u.profile_picture, u.created_at
         FROM tokens t
         INNER JOIN users u ON u.id = t.user_id
         WHERE t.token = :token
         LIMIT 1'
    );
    $stmt->execute(['token' => $token]);
    $user = $stmt->fetch();

    return $user ?: null;
}

function requireAuth(PDO $pdo): array
{
    $token = getBearerToken();
    if ($token === null) {
        jsonError('Authorization token required', 401);
    }

    $user = validateToken($pdo, $token);
    if ($user === null) {
        jsonError('Invalid or expired token', 401);
    }

    return $user;
}

function createToken(PDO $pdo, int $userId): string
{
    $token = bin2hex(random_bytes(32));
    $stmt = $pdo->prepare('INSERT INTO tokens (user_id, token) VALUES (:user_id, :token)');
    $stmt->execute(['user_id' => $userId, 'token' => $token]);
    return $token;
}

function revokeToken(PDO $pdo, string $token): bool
{
    $stmt = $pdo->prepare('DELETE FROM tokens WHERE token = :token');
    $stmt->execute(['token' => $token]);
    return $stmt->rowCount() > 0;
}
