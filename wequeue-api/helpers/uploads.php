<?php

declare(strict_types=1);

if (!function_exists('deleteUploadedImage')) {

    function deleteUploadedImage(?string $imageUrl): void
    {
        if ($imageUrl === null || trim($imageUrl) === '') {
            return;
        }

        $path = parse_url($imageUrl, PHP_URL_PATH);
        if (!is_string($path) || strpos($path, '/uploads/') === false) {
            return;
        }

        $filename = basename($path);
        if ($filename === '' || $filename === '.' || $filename === '..') {
            return;
        }

        $uploadsDir = realpath(__DIR__ . '/../uploads');
        if ($uploadsDir === false) {
            return;
        }

        $file = $uploadsDir . DIRECTORY_SEPARATOR . $filename;
        if (is_file($file)) {
            @unlink($file);
        }
    }
}
