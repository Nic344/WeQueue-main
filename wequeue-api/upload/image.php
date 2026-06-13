<?php

declare(strict_types=1);

require_once __DIR__ . '/../config/database.php';
require_once __DIR__ . '/../helpers/response.php';
require_once __DIR__ . '/../helpers/auth.php';

sendCorsHeaders();
requireMethod('POST');

$pdo = getDb();

requireAuth($pdo);

if (!isset($_FILES['file']) || !is_array($_FILES['file'])) {
    jsonError('No file uploaded (expected multipart field "file")', 422);
}

$file = $_FILES['file'];

if (($file['error'] ?? UPLOAD_ERR_NO_FILE) !== UPLOAD_ERR_OK) {
    jsonError('Upload failed (error code ' . ($file['error'] ?? -1) . ')', 422);
}

$maxBytes = 5 * 1024 * 1024;
if (($file['size'] ?? 0) <= 0 || $file['size'] > $maxBytes) {
    jsonError('File must be between 1 byte and 5 MB', 422);
}

$finfo = new finfo(FILEINFO_MIME_TYPE);
$mime = $finfo->file($file['tmp_name']);

$allowed = [
    'image/jpeg' => 'jpg',
    'image/png' => 'png',
    'image/webp' => 'webp',
];

if (!isset($allowed[$mime])) {
    jsonError('Only JPEG, PNG, or WEBP images are allowed', 422);
}

$extension = $allowed[$mime];

$uploadDir = __DIR__ . '/../uploads';
if (!is_dir($uploadDir) && !mkdir($uploadDir, 0775, true) && !is_dir($uploadDir)) {
    jsonError('Server cannot create upload directory', 500);
}

$filename = sprintf('img_%s_%s.%s', date('Ymd_His'), bin2hex(random_bytes(6)), $extension);
$target = $uploadDir . '/' . $filename;

if (!move_uploaded_file($file['tmp_name'], $target)) {
    jsonError('Failed to store uploaded file', 500);
}

$scheme = (!empty($_SERVER['HTTPS']) && strtolower((string) $_SERVER['HTTPS']) !== 'off') ? 'https' : 'http';
$host = $_SERVER['HTTP_HOST'] ?? 'localhost';
$scriptDir = str_replace('\\', '/', dirname((string) ($_SERVER['SCRIPT_NAME'] ?? '')));

$baseDir = rtrim(dirname($scriptDir), '/');
$url = $scheme . '://' . $host . $baseDir . '/uploads/' . $filename;

jsonSuccess(['url' => $url], 'Image uploaded');
