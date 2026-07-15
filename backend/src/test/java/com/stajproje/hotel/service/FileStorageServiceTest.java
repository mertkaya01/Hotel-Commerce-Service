package com.stajproje.hotel.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Dosya yukleme guvenligi: sadece resim kabul edilmeli ve kullanicidan gelen
 * dosya adi ASLA diske yazilmamali (path traversal / uzerine yazma riski).
 */
class FileStorageServiceTest {

    @TempDir
    Path tempDir;

    private FileStorageService service;

    @BeforeEach
    void setUp() {
        service = new FileStorageService(tempDir.toString());
        service.createDirectory();
    }

    private MockMultipartFile image(String filename, String contentType) {
        return new MockMultipartFile("file", filename, contentType, new byte[]{1, 2, 3});
    }

    @Test
    void store_shouldSaveFile_andReturnUploadsUrl() {
        String url = service.store(image("otelim.png", "image/png"));

        assertThat(url).startsWith("/uploads/").endsWith(".png");

        Path saved = tempDir.resolve(url.substring("/uploads/".length()));
        assertThat(Files.exists(saved)).isTrue();
    }

    @Test
    void store_shouldNotUseOriginalFilename() {
        String url = service.store(image("otelim.png", "image/png"));

        // kullanicinin verdigi ad kullanilmamali -> rastgele UUID
        assertThat(url).doesNotContain("otelim");
    }

    @Test
    void store_shouldGenerateUniqueNames_forSameFilename() {
        String first = service.store(image("ayni.png", "image/png"));
        String second = service.store(image("ayni.png", "image/png"));

        // ayni adli iki dosya birbirinin uzerine yazmamali
        assertThat(first).isNotEqualTo(second);
        assertThat(tempDir.toFile().list()).hasSize(2);
    }

    @Test
    void store_shouldRejectPathTraversalFilename() {
        // Kotu niyetli ad: ".." ile ust klasore cikmaya calisiyor.
        // Uzanti gecersiz oldugu icin reddedilir; dosya adi zaten kullanilmaz.
        assertThatThrownBy(() -> service.store(image("../../hack.exe", "image/png")))
                .isInstanceOf(IllegalArgumentException.class);

        assertThat(tempDir.toFile().list()).isEmpty();
    }

    @Test
    void store_shouldRejectNonImageContentType() {
        assertThatThrownBy(() -> service.store(image("virus.png", "application/x-msdownload")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Sadece resim");
    }

    @Test
    void store_shouldRejectDisallowedExtension_evenWithImageContentType() {
        // content-type taklit edilebilir; uzanti da dogrulanmali
        assertThatThrownBy(() -> service.store(image("kotu.svg", "image/png")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void store_shouldRejectEmptyFile() {
        MockMultipartFile empty = new MockMultipartFile("file", "bos.png", "image/png", new byte[0]);

        assertThatThrownBy(() -> service.store(empty))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void store_shouldAcceptAllAllowedImageTypes() {
        assertThat(service.store(image("a.jpg", "image/jpeg"))).endsWith(".jpg");
        assertThat(service.store(image("b.jpeg", "image/jpeg"))).endsWith(".jpeg");
        assertThat(service.store(image("c.webp", "image/webp"))).endsWith(".webp");
        assertThat(service.store(image("d.gif", "image/gif"))).endsWith(".gif");
    }
}
