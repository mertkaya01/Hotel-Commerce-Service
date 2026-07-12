# Otel Rezervasyon Sistemi — Proje Planı

## 1. Genel Mimari

```
Angular (SPA)  --REST/JSON-->  Spring Boot (API)  --JPA-->  H2 (relational data)
                                        |
                                        +--SolrJ-->  Apache Solr (arama/facet)
                                        |
                                        +--Spring Security + JWT (auth)
```

- Angular sadece API tüketir, sunucu tarafı render yok.
- Solr; arama, filtreleme (facet), autocomplete ve geo-search için kullanılır. H2 ise ana veri deposu (User, Hotel, Room, Reservation) ve tekil kayıt okuma/yazma için kullanılır. Otel arama sorguları Solr'a, rezervasyon/kullanıcı işlemleri H2'ye gider.
- Solr indexi Hotel verisinden (CSV import sırasında) beslenir; Room/Reservation Solr'a girmez, onlar tamamen H2/JPA tarafında kalır.

## 2. Teknoloji Stack

| Katman | Teknoloji |
|---|---|
| Backend | Java 17+, Spring Boot 3.x, Spring Web, Spring Data JPA |
| Güvenlik | Spring Security, JWT (jjwt veya java-jwt) |
| Veritabanı | H2 (file-based, dev/demo) |
| Arama motoru | Apache Solr (Docker container, SolrJ client) |
| Frontend | Angular (son LTS), Angular Material veya Tailwind |
| Konteynerleştirme | Docker + docker-compose (Solr + opsiyonel tüm stack) |
| Dokümantasyon | springdoc-openapi (Swagger UI) |
| Test | JUnit 5, Mockito |

## 3. Backend Katman/Paket Yapısı

```
com.stajproje.hotel
 ├── config/          (SecurityConfig, JwtConfig, SolrConfig, CorsConfig)
 ├── entity/          (User, Role, Hotel, Room, Reservation)
 ├── repository/      (JPA repository'ler + SolrHotelRepository)
 ├── dto/             (request/response DTO'ları)
 ├── service/         (iş mantığı)
 ├── controller/      (REST controller'lar)
 ├── security/         (JwtFilter, JwtProvider, UserDetailsServiceImpl)
 ├── exception/       (GlobalExceptionHandler, custom exception'lar)
 └── batch/           (CsvImportRunner — CSV -> H2 + Solr yükleme job'ı)
```

## 4. Entity Tasarımı

**User**
- id, email (unique), password (hashed), firstName, lastName, role (USER/ADMIN), createdAt

**Hotel** (dataset kolonlarından türetilir)
- id, hotelCode, name, countryCode, countryName, cityCode, cityName, rating (enum: OneStar..FiveStar), address, description, facilities, latitude, longitude, phoneNumber, websiteUrl

**Room**
- id, hotel (ManyToOne), roomType (SINGLE/DOUBLE/SUITE...), capacity, pricePerNight, totalCount (o tipten kaç oda var)

**Reservation**
- id, user (ManyToOne), room (ManyToOne), checkIn, checkOut, status (CONFIRMED/CANCELLED), createdAt
- Kısıtlama: aynı room için tarih aralığı çakışan CONFIRMED rezervasyon oluşturulamaz (service katmanında kontrol).

> Not: Dataset'te oda/fiyat bilgisi yok — Room verisi bizim üreteceğimiz (senkron/rastgele ama makul) veri olacak. Bu da projede "gerçek dünyada eksik veriyi nasıl tamamladım" diye anlatılabilecek bir karar.

## 5. API Endpoint Taslağı

**Auth**
- `POST /api/auth/register`
- `POST /api/auth/login` → JWT (+refresh token opsiyonel)

**Hotel / Search (Solr destekli)**
- `GET /api/hotels/search?q=&country=&city=&rating=&facilities=&page=&size=` → facet + sonuç listesi
- `GET /api/hotels/{id}` → detay (H2'den)
- `GET /api/hotels/autocomplete?q=` → Solr suggester

**Room**
- `GET /api/hotels/{id}/rooms`

**Reservation**
- `POST /api/reservations` (roomId, checkIn, checkOut)
- `GET /api/reservations/me`
- `DELETE /api/reservations/{id}` (iptal)

**Profile**
- `GET /api/users/me`
- `PUT /api/users/me`

## 6. Veri Pipeline (CSV → H2 + Solr)

### Veri temizleme kuralları (örneklem taramasından çıkan gerçek bulgular)
- `HotelRating`: değerlerin ~%35'i geçerli bir yıldız değeri değil, `"All"` gibi anlamsız metin taşıyor → tanınmayan her değer `HotelRating.UNRATED`'e eşlenecek.
- `PhoneNumber` (%33.6 boş), `HotelWebsiteUrl` (%24.3 boş), `Description`/`HotelFacilities` (~%11 boş) → opsiyonel alanlar, boşsa `null` olarak bırakılacak, import'u durdurmayacak.
- `Attractions`, `FaxNumber` → modelde hiç yer almıyor, import'ta okunmayacak.
- `Map` kolonu (lat/long) → neredeyse hiç boş değil (~%0), parse hatası olursa o satır atlanabilir.
- Türkiye'de 951 otel var (tam sayım) → demo subset'inde mutlaka yer alacak.

1. `hotels.csv` (1M+ satır) içinden birkaç ülke seçilip (ör. Türkiye + birkaç Avrupa ülkesi) birkaç bin satırlık bir alt küme filtrelenir.
2. Bir `CsvImportRunner` (Spring `CommandLineRunner`) bu alt kümeyi okuyup H2'ye Hotel kayıtları olarak yazar.
3. Aynı job, Hotel kayıtlarını Solr'a da indexler (SolrJ ile).
4. Her Hotel için otomatik 2-4 adet Room üretilir (tip/fiyat/kapasite rastgele ama mantıklı aralıklarda).
5. Bu adım idempotent olmalı (uygulama her açıldığında veri tekrar yüklenmemeli — DB boşsa yükle mantığı).

## 7. Frontend (Angular) Sayfa Listesi

- `/login`, `/register`
- `/hotels` — arama + facet filtreleri (ülke, şehir, yıldız, tesis) + sayfalama
- `/hotels/:id` — otel detayı, oda listesi, tarih seçici, rezervasyon butonu
- `/profile` — kullanıcı bilgisi + rezervasyonlarım (iptal edilebilir)
- (opsiyonel) `/admin` — otel/oda yönetimi

## 8. Fazlar (Roadmap)

1. **Faz 1 — Backend temeli:** Spring Boot proje kurulumu, entity'ler, H2 bağlantısı, Spring Security + JWT (register/login), global exception handling.
2. **Faz 2 — Veri pipeline + Solr:** CSV filtreleme scripti, CsvImportRunner, Solr schema tanımı, Docker ile Solr ayağa kaldırma, arama endpoint'i.
3. **Faz 3 — Room & Reservation:** Room üretimi, rezervasyon CRUD, tarih çakışma kontrolü, profile endpoint'leri.
4. **Faz 4 — Angular temel:** proje iskeleti, auth sayfaları, JWT interceptor, routing/guard.
5. **Faz 5 — Hotel arama UI + rezervasyon akışı:** arama sayfası, facet filtre UI, otel detay, tarih seçici, profil sayfası.
6. **Faz 6 — Cila:** Swagger/OpenAPI, backend testleri, docker-compose (tüm stack tek komut), README + mimari diyagram, (opsiyonel) admin panel.

## 9. Kurulum Gereksinimleri

| Araç | Neden | Not |
|---|---|---|
| JDK 17 veya 21 | Spring Boot 3.x için gerekli | Adoptium/Temurin önerilir |
| IntelliJ IDEA (Community yeterli) | Backend geliştirme | Ultimate'ta Spring desteği daha iyi ama zorunlu değil |
| Maven | Backend build | IntelliJ ile birlikte gelir |
| Node.js (LTS) + npm | Angular için gerekli | |
| Angular CLI | `npm install -g @angular/cli` | |
| Docker Desktop | Solr'u container olarak çalıştırmak için | `docker pull solr` yeterli, ayrıca Solr indirmeye gerek yok |
| Git | Versiyon kontrolü | |
| Postman (opsiyonel) | API test | |

H2 için ayrı bir kurulum gerekmiyor — Maven bağımlılığı olarak gelir, embedded/file-based çalışır.
