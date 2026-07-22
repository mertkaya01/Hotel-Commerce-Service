package com.stajproje.hotel.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * E-posta gönderimi — Brevo (sendinblue) transactional API üzerinden (HTTPS, port
 * engeli yok). En iyi çaba (best-effort): gönderim başarısız olsa da uygulama akışını
 * (örn. kayıt) ASLA bozmaz — @Async ile arka planda çalışır, hata sadece loglanır.
 *
 * BREVO_API_KEY tanımlı DEĞİLSE gerçek mail gönderilmez; bunun yerine doğrulama
 * bağlantısı log'a basılır (yerel geliştirme / anahtarsız ortam için). Böylece akış
 * anahtar olmadan da uçtan uca test edilebilir.
 */
@Slf4j
@Service
public class EmailService {

    private static final String BREVO_URL = "https://api.brevo.com/v3/smtp/email";

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    @Value("${brevo.api-key:}")
    private String apiKey;

    @Value("${brevo.sender-email:}")
    private String senderEmail;

    @Value("${brevo.sender-name:Otel Rezervasyon}")
    private String senderName;

    @Async
    public void sendVerificationEmail(String toEmail, String toName, String verifyLink) {
        String subject = "E-posta adresini doğrula — Otel Rezervasyon";
        String html = """
                <div style="font-family:Arial,sans-serif;max-width:520px;margin:auto">
                  <h2 style="color:#065A82">Hoş geldin%s!</h2>
                  <p>Otel Rezervasyon hesabını oluşturduğun için teşekkürler. Hesabını
                     doğrulamak için aşağıdaki butona tıkla:</p>
                  <p style="text-align:center;margin:28px 0">
                    <a href="%s" style="background:#065A82;color:#fff;text-decoration:none;
                       padding:12px 24px;border-radius:8px;display:inline-block">
                      E-postamı doğrula
                    </a>
                  </p>
                  <p style="color:#666;font-size:13px">Buton çalışmazsa bu bağlantıyı tarayıcına yapıştır:<br>%s</p>
                  <p style="color:#999;font-size:12px">Bu bağlantı 24 saat geçerlidir. Bu kaydı sen yapmadıysan bu e-postayı yok sayabilirsin.</p>
                </div>
                """.formatted(toName != null && !toName.isBlank() ? " " + toName : "", verifyLink, verifyLink);

        if (apiKey == null || apiKey.isBlank() || senderEmail == null || senderEmail.isBlank()) {
            // Anahtarsiz ortam: gercek mail atma, linki logla (yerel test icin)
            log.warn("BREVO_API_KEY/SENDER tanimli degil - dogrulama maili GONDERILMEDI. "
                    + "Dogrulama linki (manuel test icin): {}", verifyLink);
            return;
        }

        try {
            String body = """
                    {
                      "sender": {"name": %s, "email": %s},
                      "to": [{"email": %s, "name": %s}],
                      "subject": %s,
                      "htmlContent": %s
                    }
                    """.formatted(
                    jsonStr(senderName), jsonStr(senderEmail),
                    jsonStr(toEmail), jsonStr(toName == null ? "" : toName),
                    jsonStr(subject), jsonStr(html));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BREVO_URL))
                    .timeout(Duration.ofSeconds(10))
                    .header("api-key", apiKey)
                    .header("Content-Type", "application/json")
                    .header("accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() / 100 == 2) {
                log.info("Dogrulama maili gonderildi: {}", toEmail);
            } else {
                log.warn("Brevo mail gonderimi basarisiz ({}): {}", response.statusCode(), response.body());
            }
        } catch (Exception e) {
            // Kayit akisini ASLA bozma - sadece logla
            log.warn("Dogrulama maili gonderilemedi ({}): {}", toEmail, e.getMessage());
        }
    }

    /** Minimal JSON string kacislama (ozel karakterleri guvenli hale getirir). */
    private String jsonStr(String s) {
        if (s == null) return "\"\"";
        StringBuilder sb = new StringBuilder("\"");
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
                }
            }
        }
        return sb.append("\"").toString();
    }
}
