package com.example.queueapp.util;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public final class ImageUploadHelper {

    private static final String DEFAULT_MIME = "image/jpeg";

    private ImageUploadHelper() {
    }

    @NonNull
    public static MultipartBody.Part createImagePart(@NonNull Context context, @NonNull Uri uri)
            throws IOException {
        ContentResolver resolver = context.getContentResolver();

        String mime = resolver.getType(uri);
        if (mime == null || mime.isEmpty()) {
            mime = DEFAULT_MIME;
        }

        byte[] bytes = readAllBytes(resolver, uri);
        if (bytes.length == 0) {
            throw new IOException("Selected image is empty or unreadable");
        }

        MediaType mediaType = MediaType.parse(mime);
        RequestBody body = RequestBody.create(mediaType, bytes);

        String fileName = "upload_" + System.currentTimeMillis() + "." + extensionFor(mime);
        return MultipartBody.Part.createFormData("file", fileName, body);
    }

    private static byte[] readAllBytes(ContentResolver resolver, Uri uri) throws IOException {
        try (InputStream in = resolver.openInputStream(uri)) {
            if (in == null) {
                throw new IOException("Cannot open the selected image");
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            return out.toByteArray();
        }
    }

    private static String extensionFor(String mime) {
        switch (mime) {
            case "image/png":
                return "png";
            case "image/webp":
                return "webp";
            default:
                return "jpg";
        }
    }
}
