<?php

declare(strict_types=1);

if (!function_exists('deleteUploadedImage')) {
    /**
     * Deletes a locally uploaded image file referenced by $imageUrl. Only files
     * inside this API's /uploads directory are touched; external URLs are ignored.
     * Path traversal is prevented via basename() + realpath() containment check.
     */
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
