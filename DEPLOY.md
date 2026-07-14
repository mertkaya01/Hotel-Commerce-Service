# 🚀 Canlıya Alma (Deploy) Rehberi

Mimari: **Frontend → Vercel**, **Backend + Solr → Render**.

> Neden Vercel tek başına yetmez? Vercel yalnızca statik siteleri (Angular build çıktısı)
> çalıştırır; Java (Spring Boot) ve Solr gibi sunucu süreçlerini çalıştıramaz. Bu yüzden
> backend + Solr'u Render'a koyuyoruz.

Frontend, API isteklerini Vercel üzerinden Render backend'e **proxy'ler** (`frontend/vercel.json`),
böylece tarayıcı her şeyi aynı origin'den görür ve CORS derdi olmaz.

---

## Bölüm 1 — Backend + Solr (Render)

1. https://render.com → GitHub ile giriş yap.
2. **New +** → **Blueprint** → bu repoyu (`Hotel-Commerce-Service`) seç. Render, kökteki
   `render.yaml`'ı okuyup **iki servis** oluşturur: `hotel-solr` ve `hotel-backend`.
3. İlk oluşturmada iki alanı boş bırak (birazdan dolduracağız): `SOLR_BASE_URL`, `APP_CORS_ORIGINS`.
4. **Önce `hotel-solr` deploy olsun.** Deploy bitince adresini kopyala (ör.
   `https://hotel-solr-ab12.onrender.com`).
5. `hotel-backend` servisine gir → **Environment**:
   - `SOLR_BASE_URL` = `https://hotel-solr-ab12.onrender.com/solr` (kendi Solr adresin + `/solr`)
   - `APP_CORS_ORIGINS` = (Vercel adresini Bölüm 2'de alınca gireceksin; şimdilik boş/placeholder)
   - **Save** → backend yeniden deploy olur. İlk açılışta ~5000 oteli H2 + Solr'a yükler (30-60 sn).
6. Backend adresini not al (ör. `https://hotel-backend-cd34.onrender.com`).
   - Test: tarayıcıda `https://hotel-backend-cd34.onrender.com/api/hotels/search?size=1` → JSON dönmeli.

## Bölüm 2 — Frontend (Vercel)

1. `frontend/vercel.json` içindeki `RENDER_BACKEND_URL.onrender.com`'u **kendi backend adresinle** değiştir:
   ```json
   "destination": "https://hotel-backend-cd34.onrender.com/api/$1"
   ```
   Değiştir, commit et, push et.
2. https://vercel.com → GitHub ile giriş → **Add New → Project** → bu repoyu seç.
3. **Root Directory: `frontend`** olarak ayarla (ÖNEMLİ — proje `frontend/` alt klasöründe).
4. Framework otomatik **Angular** algılanır. **Deploy**'a bas.
5. Deploy bitince Vercel sana bir adres verir: `https://senin-projen.vercel.app` → **canlı linkin!**

## Bölüm 3 — Son bağlantı (CORS)

Aslında Vercel proxy sayesinde CORS gerekmez. Ama frontend'i ileride doğrudan backend'e
bağlarsan: Render'da `hotel-backend` → `APP_CORS_ORIGINS` = `https://senin-projen.vercel.app`
yapıp kaydet.

---

## 👤 Hazır hesap
- Platform yöneticisi: `admin@otel.com` / `admin1234` (açılışta otomatik oluşur).

## ⚠️ Ücretsiz katman notları (dürüstçe)
- **Uyku modu:** Render ücretsiz servisler ~15 dk hareketsizlikte uyur. İlk istek servisleri
  uyandırır → ilk açılış 30-60 sn yavaş olabilir. Sonra hızlanır.
- **Veri sıfırlanması:** Ücretsiz planda kalıcı disk yok → backend her yeniden deploy'da H2'yi
  sıfırlar; oteller `hotels_subset.csv`'den yeniden yüklenir, admin yeniden oluşur. Kullanıcı
  kayıtları/eklenen oteller sıfırlanır (demo için sorun değil).
- **Bellek:** Solr + Spring Boot 512MB'a sığsın diye heap sınırlıdır (`SOLR_HEAP=300m`,
  `-Xmx350m`). OOM olursa Render'da servise daha yüksek plan (Starter) verebilir ya da
  `hotels_subset.csv`'yi küçültebilirsin.

## Sorun giderme
- **Aramada sonuç yok:** backend `SOLR_BASE_URL` doğru mu? Solr servisi ayakta mı (uyandı mı)?
- **Frontend'de veri gelmiyor:** `vercel.json` içindeki backend URL doğru mu? Backend'i tarayıcıda
  `/api/hotels/search?size=1` ile test et.
- **Solr $PORT'ta açılmadı:** Render Solr logunda portu kontrol et; gerekirse servis komutunu
  `bash -c "export SOLR_PORT=$PORT && solr-precreate hotels"` olarak doğrula.
