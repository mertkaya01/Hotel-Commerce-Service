# 🏨 Otel Rezervasyon Sistemi

Gerçek bir otel veri seti (1M+ satır) üzerine kurulu, **tam yığın (full-stack)** otel arama ve rezervasyon uygulaması. Apache Solr ile gelişmiş arama, JWT tabanlı güvenlik, tarih çakışması kontrollü rezervasyon akışı.

> Staj portföy projesi. Amaç: yalnızca çalışan bir demo değil, her kararın _neden_ öyle verildiğini anlatabilecek derinlikte bir sistem.

---

## 🧱 Mimari

```
┌──────────────┐   REST / JSON    ┌────────────────────┐   JPA/JDBC   ┌──────────┐
│   Angular    │ ───────────────► │   Spring Boot API  │ ───────────► │    H2    │
│  (SPA, 4200) │ ◄─────────────── │      (8080)        │              │ (users,  │
└──────────────┘                  │                    │              │  hotels, │
                                  │  Spring Security   │              │  rooms,  │
                                  │      + JWT         │              │  reserv.)│
                                  │                    │              └──────────┘
                                  │                    │   SolrJ      ┌──────────┐
                                  │                    │ ───────────► │  Apache  │
                                  │                    │              │   Solr   │
                                  └────────────────────┘              │  (8983)  │
                                                                      └──────────┘
```

- **H2** ana veri deposu (kullanıcı, otel, oda, rezervasyon) — tekil kayıt okuma/yazma.
- **Solr** yalnızca otel araması için — full-text, facet (ülke/şehir/yıldız), büyük/küçük harf duyarsız şehir araması.
- İki kaynak da CSV import sırasında beslenir; H2 kalıcı kaynak, Solr ondan yeniden indexlenebilir.

---

## 🛠️ Teknoloji Yığını

| Katman | Teknoloji |
|---|---|
| Backend | Java 21, Spring Boot 3.5, Spring Web, Spring Data JPA |
| Güvenlik | Spring Security, JWT (jjwt) |
| Veritabanı | H2 (dosya tabanlı) |
| Arama | Apache Solr 9.10 (SolrJ client) |
| Frontend | Angular 22 (standalone components + signals), Angular Material |
| Dokümantasyon | springdoc-openapi (Swagger UI) |
| Test | JUnit 5 + Mockito |
| Konteyner | Docker + docker-compose |

---

## 🚀 Çalıştırma

> Not: Ham veri seti (2.4 GB) repoda **yoktur**; uygulama repoya dahil olan
> `backend/src/main/resources/data/hotels_subset.csv` (~5000 otel) ile kutudan çıktığı
> gibi çalışır. Ekstra indirme gerekmez.

### Seçenek 1 — Docker (tek komut, önerilen)

```bash
git clone <REPO_URL>
cd STAJ
docker compose up --build
```

- Frontend: http://localhost:4200
- Backend API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui/index.html
- Solr admin: http://localhost:8983

İlk açılışta backend, `hotels_subset.csv`'den ~5000 oteli H2'ye ve Solr'a yükler (birkaç saniye).

### Seçenek 2 — Yerel geliştirme

**Gereksinimler:** JDK 21, Node.js 20+, Docker (yalnızca Solr için).

```bash
# 1) Solr'u başlat
docker compose up -d solr

# 2) Backend (yeni terminal)
cd backend
./mvnw spring-boot:run        # http://localhost:8080

# 3) Frontend (yeni terminal)
cd frontend
npm install
npm start                      # http://localhost:4200
```

### 🍎 macOS (Apple Silicon / M1–M2) kurulumu

Tüm imajlar (Solr, Temurin JDK, Node, Nginx) arm64 destekler; Docker yolu M2'de sorunsuz çalışır.

```bash
# Gereksinimler (Homebrew ile)
brew install --cask docker        # Docker Desktop (bir kez aç, arka planda çalışsın)
brew install openjdk@21 node      # yalnızca yerel geliştirme için

# Projeyi al ve tek komutla çalıştır
git clone <REPO_URL>
cd STAJ
docker compose up --build         # http://localhost:4200
```

Yerel geliştirmede JDK 21'i PATH'e almak için (Apple Silicon):
```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
```

### 👤 Hazır hesaplar

- **Platform yöneticisi** (ev sahibi başvurularını değerlendirir):
  `admin@otel.com` / `admin1234` — uygulama açılışında otomatik oluşturulur.
- Normal kullanıcı için **Kayıt Ol** ile hesap açabilirsin.

---

## 🔍 Öne Çıkan Teknik Kararlar

Bu projeyi sıradan bir CRUD demosundan ayıran, bilinçli mühendislik kararları:

- **Solr'da `string` + `text` ikiz alanlar.** `cityName` string olarak tutulur (facet ve exact filtre için), `cityText` ise text_general olarak (büyük/küçük harf duyarsız arama için). "berlin" yazınca, adında "berlin" geçmese bile Berlin şehrindeki tüm oteller gelir.
- **Kendi kendini onaran Solr reindex.** H2 kalıcı kaynaktır; Solr indeksi silinse bile uygulama açılışta H2'den yeniden indexler (`SolrReindexRunner`), kullanıcı/rezervasyon verisi kaybolmaz.
- **Tarih çakışma kontrolü.** Aynı odaya çakışan tarihlerde ikinci rezervasyon engellenir — klasik "overlapping interval" problemi tek JPQL sorgusuyla: `checkIn < :checkOut AND checkOut > :checkIn`.
- **IDOR koruması.** Kullanıcı ID'si URL/body'den değil, doğrulanmış JWT'den alınır; başkasının rezervasyonu iptal edilemez (403).
- **Stateless JWT auth.** Sunucu session tutmaz; her istek token ile kendi kendine yeter. Yanlış şifre ile "kayıtlı email" ayrımı verilmez (user enumeration'a karşı).
- **Gerçek veri, gerçek sorunlar.** Kaynak CSV `cp1252` kodlu (UTF-8 değil), yinelenen `HotelCode`'lar ve `"All"` gibi bozuk yıldız değerleri içeriyor — hepsi import pipeline'ında temizleniyor.

> **Not (demo veri):** Veri setinde otel fotoğrafı, kullanıcı yorumu veya fiyat yok. Kart görselleri örnek (Unsplash) fotoğraflar; yorum sayısı/puanı ve gecelik fiyat, otel koduna göre deterministik üretilen demo değerlerdir.

---

## 📚 API Uç Noktaları

Tam ve interaktif liste: **Swagger UI** (`/swagger-ui/index.html`).

| Metot | Yol | Açıklama | Auth |
|---|---|---|---|
| POST | `/api/auth/register` | Kayıt | ✗ |
| POST | `/api/auth/login` | Giriş (JWT döner) | ✗ |
| GET | `/api/hotels/search` | Arama + facet (q, country, city, rating, page, size) | ✗ |
| GET | `/api/hotels/{hotelCode}` | Otel detayı | ✗ |
| GET | `/api/hotels/{hotelCode}/rooms` | Oda listesi | ✗ |
| POST | `/api/reservations` | Rezervasyon oluştur | ✓ |
| GET | `/api/reservations/me` | Rezervasyonlarım | ✓ |
| DELETE | `/api/reservations/{id}` | Rezervasyon iptal | ✓ |
| GET/PUT | `/api/users/me` | Profil | ✓ |

---

## 🧪 Test

```bash
cd backend
./mvnw test
```

Testler dış altyapıya (Solr) bağımlı değildir — `test` profilinde in-memory H2 kullanılır, arama/import bileşenleri devre dışı bırakılır. Odak: rezervasyon çakışma mantığı, IDOR koruması, şifre hash'leme.

---

## 📁 Proje Yapısı

```
STAJ/
├── backend/          Spring Boot API
│   └── src/main/java/com/stajproje/hotel/
│       ├── entity/       JPA entity'leri
│       ├── repository/   Spring Data repository'leri
│       ├── service/      iş mantığı
│       ├── controller/   REST controller'lar
│       ├── security/     JWT filtresi, provider, UserDetails
│       ├── solr/         Solr şema + indexleme
│       ├── batch/        CSV import + reindex runner'ları
│       ├── dto/          request/response DTO'ları
│       ├── exception/    global exception handler
│       └── config/       Security, Solr, OpenAPI, CORS
├── frontend/         Angular SPA
│   └── src/app/
│       ├── core/         servisler, guard, interceptor, model
│       ├── features/     sayfalar (home, hotel-detail, auth, profile, reservations)
│       └── shared/       navbar, hotel-card, star-rating
├── docker-compose.yml
└── PROJE_PLANI.md    detaylı mimari + faz planı
```
