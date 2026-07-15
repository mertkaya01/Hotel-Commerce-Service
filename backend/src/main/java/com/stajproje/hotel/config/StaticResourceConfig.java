package com.stajproje.hotel.config;

import com.stajproje.hotel.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Yuklenen fotograflari /uploads/** adresinden servis eder.
 * (Diskteki app.upload.dir klasorunu web'e acar.)
 */
@Configuration
@RequiredArgsConstructor
public class StaticResourceConfig implements WebMvcConfigurer {

    private final FileStorageService fileStorageService;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(fileStorageService.getUploadDir().toUri().toString());
    }
}
