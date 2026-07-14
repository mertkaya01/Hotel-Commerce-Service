/**
 * Otel kartlarının görsel zenginliği için yardımcılar.
 *
 * ÖNEMLI: Kullandığımız veri setinde otel fotoğrafı, kullanıcı yorumu veya
 * gecelik fiyat özeti YOK. Aşağıdaki fotoğraf/yorum/fiyat değerleri DEMO amaçlıdır;
 * her otel için `hotelCode`'dan deterministik üretilir (yani sayfa her yenilendiğinde
 * aynı kalır, titremez). Gerçek fiyatı ileride Room verisinden bağlayabiliriz.
 */

// Doğrulanmış (HTTP 200) Unsplash otel/seyahat fotoğrafları
const HOTEL_PHOTO_IDS = [
  'photo-1566073771259-6a8506099945',
  'photo-1551882547-ff40c63fe5fa',
  'photo-1520250497591-112f2f40a3f4',
  'photo-1571003123894-1f0594d2b5d9',
  'photo-1582719478250-c89cae4dc85b',
  'photo-1568084680786-a84f91d1153c',
  'photo-1618773928121-c32242e63f39',
  'photo-1445019980597-93fa8acb246c',
  'photo-1590490360182-c33d57733427',
  'photo-1611892440504-42a792e24d32',
  'photo-1542314831-068cd1dbfeeb',
  'photo-1522708323590-d24dbb6b0267',
];

const RATING_PRICE_BASE: Record<string, number> = {
  FIVE_STAR: 320,
  FOUR_STAR: 180,
  THREE_STAR: 110,
  TWO_STAR: 70,
  ONE_STAR: 45,
  UNRATED: 90,
};

// basit, deterministik string hash (djb2 benzeri)
function hashCode(input: string): number {
  let hash = 5381;
  for (let i = 0; i < input.length; i++) {
    hash = (hash * 33) ^ input.charCodeAt(i);
  }
  return Math.abs(hash);
}

export function hotelPhoto(hotelCode: string): string {
  const id = HOTEL_PHOTO_IDS[hashCode(hotelCode) % HOTEL_PHOTO_IDS.length];
  return `https://images.unsplash.com/${id}?w=600&h=380&fit=crop&q=80`;
}

/**
 * Otel eklerken seçilebilecek hazır DEMO fotoğraf galerisi.
 * (Gerçek dosya yükleme yerine geçici çözüm: ev sahibi buradan tıklayıp seçer.)
 */
export const DEMO_HOTEL_PHOTOS: string[] = HOTEL_PHOTO_IDS.map(
  (id) => `https://images.unsplash.com/${id}?w=800&h=600&fit=crop&q=80`,
);

/** DEMO ortalama gecelik fiyat (yıldıza göre taban + otele özel sapma) */
export function demoNightlyPrice(hotelCode: string, rating: string): number {
  const base = RATING_PRICE_BASE[rating] ?? RATING_PRICE_BASE['UNRATED'];
  const variance = (hashCode(hotelCode + 'p') % 60) - 20; // -20..+39
  return Math.round((base + variance) / 5) * 5;
}

/** DEMO yorum sayısı (otele özel, sabit) */
export function demoReviewCount(hotelCode: string): number {
  return 80 + (hashCode(hotelCode + 'r') % 1920); // 80..1999
}

/** DEMO yorum puanı 7.4 - 9.8 arası */
export function demoReviewScore(hotelCode: string): number {
  const n = hashCode(hotelCode + 's') % 25; // 0..24
  return Math.round((7.4 + n / 10) * 10) / 10;
}

export function reviewScoreLabel(score: number): string {
  if (score >= 9) return 'Mükemmel';
  if (score >= 8.5) return 'Çok İyi';
  if (score >= 8) return 'İyi';
  return 'Hoş';
}

const TAG_POOL = [
  'Ücretsiz İptal',
  'Kahvaltı Dahil',
  'Spa',
  'Ücretsiz Wi-Fi',
  'Havuz',
  'Otopark',
];

/** DEMO etiketler — otele özel, sabit 3 adet (özet veride tesis bilgisi yok) */
export function demoTags(hotelCode: string): string[] {
  const start = hashCode(hotelCode + 't') % TAG_POOL.length;
  return [0, 1, 2].map((i) => TAG_POOL[(start + i) % TAG_POOL.length]);
}

/** Otel için birden fazla galeri fotoğrafı (detay galerisi için) */
export function hotelGallery(hotelCode: string, count = 5): string[] {
  const start = hashCode(hotelCode) % HOTEL_PHOTO_IDS.length;
  return Array.from({ length: count }, (_, i) => {
    const id = HOTEL_PHOTO_IDS[(start + i) % HOTEL_PHOTO_IDS.length];
    return `https://images.unsplash.com/${id}?w=800&h=600&fit=crop&q=80`;
  });
}

export interface DemoReview {
  name: string;
  initial: string;
  score: number;
  comment: string;
  date: string;
}

const REVIEW_NAMES = ['Ahmet Y.', 'Elif K.', 'Mehmet D.', 'Zeynep A.', 'Can B.', 'Selin T.'];
const REVIEW_COMMENTS = [
  'Konumu harika, personel çok ilgiliydi. Kesinlikle tekrar geleceğim.',
  'Odalar temiz ve genişti. Kahvaltı beklentimin üzerindeydi.',
  'Fiyat/performans açısından çok başarılı. Manzara muhteşemdi.',
  'Ulaşımı kolay, çevresi sakin. Ailecek çok memnun kaldık.',
  'Spa ve havuz tertemizdi. Huzurlu bir tatil geçirdik.',
];
const REVIEW_DATES = ['Mart 2026', 'Şubat 2026', 'Ocak 2026', 'Aralık 2025', 'Kasım 2025'];

/** DEMO yorumlar — otele özel, sabit (veri setinde gerçek yorum yok) */
export function demoReviews(hotelCode: string, count = 4): DemoReview[] {
  const base = hashCode(hotelCode + 'rev');
  return Array.from({ length: count }, (_, i) => {
    const name = REVIEW_NAMES[(base + i) % REVIEW_NAMES.length];
    const score = Math.round((7.8 + ((base + i * 7) % 21) / 10) * 10) / 10;
    return {
      name,
      initial: name.charAt(0),
      score,
      comment: REVIEW_COMMENTS[(base + i) % REVIEW_COMMENTS.length],
      date: REVIEW_DATES[(base + i) % REVIEW_DATES.length],
    };
  });
}
