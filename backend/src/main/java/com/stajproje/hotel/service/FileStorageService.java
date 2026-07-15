package com.stajproje.hotel.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * Otel fotograflarini diske kaydeder ve tarayicinin erisebilecegi bir URL doner.
 *
 * NOT: Gercek bir production sisteminde dosyalar S3/GCS gibi bir object storage'a
 * konur (sunucu yeniden deploy edilince disk silinebilir). Staj projesi icin
 * yerel disk yeterli.
 */
@Service
public class FileStorageService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp", "gif");
    private static final Set<String> ALLOWED_CONTENT_TYPES =
            Set.of("image/jpeg", "image/png", "image/webp", "image/gif");

    private final Path uploadDir;

    public FileStorageService(@Value("${app.upload.dir:./uploads}") String uploadDir) {
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    @PostConstruct
    void createDirectory() {
        try {
            Files.createDirectories(uploadDir);
        } catch (IOException e) {
            throw new UncheckedIOException("Yukleme klasoru olusturulamadi: " + uploadDir, e);
        }
    }

    /** Dosyayi kaydeder, erisim URL'ini doner (orn. /uploads/ab12....jpg). */
    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Dosya bos olamaz");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException("Sadece resim yuklenebilir (jpg, png, webp, gif)");
        }

        String extension = extensionOf(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("Gecersiz dosya uzantisi: " + extension);
        }

        // Kullanicidan gelen dosya adini ASLA kullanma (path traversal riski) -> rastgele ad uret
        String storedName = UUID.randomUUID().toString().replace("-", "") + "." + extension;
        Path target = uploadDir.resolve(storedName).normalize();

        if (!target.startsWith(uploadDir)) {
            throw new IllegalArgumentException("Gecersiz dosya yolu");
        }

        try (var in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException("Dosya kaydedilemedi", e);
        }

        return "/uploads/" + storedName;
    }

    private String extensionOf(String filename) {
        if (filename == null) return "";
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) return "";
        return filename.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    public Path getUploadDir() {
        return uploadDir;
    }
}
