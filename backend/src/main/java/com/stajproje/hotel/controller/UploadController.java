package com.stajproje.hotel.controller;

import com.stajproje.hotel.dto.upload.UploadResponse;
import com.stajproje.hotel.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/uploads")
@RequiredArgsConstructor
public class UploadController {

    private final FileStorageService fileStorageService;

    /** Ev sahibi otel fotografi yukler; donen url otel kaydinda kullanilir. */
    @PostMapping(value = "/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public UploadResponse uploadPhoto(@RequestParam("file") MultipartFile file) {
        String url = fileStorageService.store(file);
        return new UploadResponse(url);
    }
}
