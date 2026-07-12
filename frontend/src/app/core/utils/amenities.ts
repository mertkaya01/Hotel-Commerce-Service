/**
 * Otel `facilities` alanı ham, boşlukla ayrılmış İngilizce bir metindir
 * (ör. "Free WiFi Sauna Fitness facilities 24-hour front desk ...").
 * Burada bu metni tarayıp bilinen olanakları anlamlı ikon + Türkçe etiket +
 * kategoriye eşliyoruz. Böylece detay sayfasındaki "Olanaklar" sekmesi
 * düz metin yerine düzenli, ikonlu bir görünüm alır.
 */

export interface Amenity {
  icon: string;
  label: string;
}

export interface AmenityGroup {
  category: string;
  items: Amenity[];
}

interface AmenityRule {
  // facilities metninde aranan anahtar kelimeler (küçük harf)
  keywords: string[];
  icon: string;
  label: string;
  category: string;
}

const RULES: AmenityRule[] = [
  // İnternet & Teknoloji
  { keywords: ['wifi', 'wi-fi', 'internet'], icon: 'wifi', label: 'Ücretsiz Wi-Fi', category: 'İnternet & Teknoloji' },
  { keywords: ['television', 'lcd', 'tv'], icon: 'tv', label: 'Televizyon', category: 'İnternet & Teknoloji' },

  // Konfor
  { keywords: ['air condition', 'climate control'], icon: 'ac_unit', label: 'Klima', category: 'Konfor' },
  { keywords: ['heating'], icon: 'thermostat', label: 'Isıtma', category: 'Konfor' },
  { keywords: ['minibar', 'refrigerator', 'mini-bar'], icon: 'kitchen', label: 'Minibar', category: 'Konfor' },
  { keywords: ['safe'], icon: 'lock', label: 'Kasa', category: 'Konfor' },
  { keywords: ['elevator', 'lift'], icon: 'elevator', label: 'Asansör', category: 'Konfor' },
  { keywords: ['24-hour front desk', '24 hour front', 'front desk'], icon: 'support_agent', label: '24 Saat Resepsiyon', category: 'Konfor' },
  { keywords: ['laundry', 'dry cleaning'], icon: 'local_laundry_service', label: 'Çamaşırhane', category: 'Konfor' },
  { keywords: ['room service'], icon: 'room_service', label: 'Oda Servisi', category: 'Konfor' },

  // Yeme & İçme
  { keywords: ['breakfast'], icon: 'bakery_dining', label: 'Kahvaltı', category: 'Yeme & İçme' },
  { keywords: ['restaurant'], icon: 'restaurant', label: 'Restoran', category: 'Yeme & İçme' },
  { keywords: ['bar', 'lounge'], icon: 'local_bar', label: 'Bar', category: 'Yeme & İçme' },
  { keywords: ['coffee', 'tea'], icon: 'coffee', label: 'Çay/Kahve', category: 'Yeme & İçme' },

  // Spa & Aktivite
  { keywords: ['pool', 'swimming'], icon: 'pool', label: 'Havuz', category: 'Spa & Aktivite' },
  { keywords: ['spa'], icon: 'spa', label: 'Spa', category: 'Spa & Aktivite' },
  { keywords: ['sauna'], icon: 'whatshot', label: 'Sauna', category: 'Spa & Aktivite' },
  { keywords: ['steam room', 'hammam', 'turkish bath'], icon: 'hot_tub', label: 'Buhar Odası', category: 'Spa & Aktivite' },
  { keywords: ['fitness', 'gym'], icon: 'fitness_center', label: 'Fitness', category: 'Spa & Aktivite' },
  { keywords: ['tennis'], icon: 'sports_tennis', label: 'Tenis Kortu', category: 'Spa & Aktivite' },
  { keywords: ['garden'], icon: 'park', label: 'Bahçe', category: 'Spa & Aktivite' },

  // Hizmetler & Ulaşım
  { keywords: ['parking', 'valet'], icon: 'local_parking', label: 'Otopark', category: 'Hizmetler & Ulaşım' },
  { keywords: ['airport shuttle', 'shuttle'], icon: 'airport_shuttle', label: 'Havaalanı Servisi', category: 'Hizmetler & Ulaşım' },
  { keywords: ['business center', 'meeting room', 'conference'], icon: 'business_center', label: 'Toplantı Salonu', category: 'Hizmetler & Ulaşım' },
  { keywords: ['babysitting', 'childcare', "children's club", 'kids club'], icon: 'child_care', label: 'Çocuk Kulübü', category: 'Hizmetler & Ulaşım' },
  { keywords: ['pet', 'dog'], icon: 'pets', label: 'Evcil Hayvan Kabul', category: 'Hizmetler & Ulaşım' },
  { keywords: ['wheelchair', 'accessible'], icon: 'accessible', label: 'Engelli Erişimi', category: 'Hizmetler & Ulaşım' },
  { keywords: ['non-smoking', 'smoke-free'], icon: 'smoke_free', label: 'Sigara İçilmez Alan', category: 'Hizmetler & Ulaşım' },
];

const CATEGORY_ORDER = [
  'Konfor',
  'İnternet & Teknoloji',
  'Yeme & İçme',
  'Spa & Aktivite',
  'Hizmetler & Ulaşım',
];

/**
 * Ham facilities metnini kategorilere ayrılmış, ikonlu olanak gruplarına çevirir.
 * Metinde bulunmayan olanaklar listelenmez.
 */
function escapeRegex(s: string): string {
  return s.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
}

// Kelime sınırıyla eşleşme: "bar" -> "Bar/lounge" evet, ama "Barbecue" hayır;
// "tea" -> "coffee/tea" evet, ama "steam" hayır.
function matchesKeyword(text: string, keyword: string): boolean {
  return new RegExp(`\\b${escapeRegex(keyword)}\\b`).test(text);
}

export function parseAmenities(facilities: string | null | undefined): AmenityGroup[] {
  if (!facilities) return [];
  const text = facilities.toLowerCase();

  const seen = new Set<string>();
  const byCategory = new Map<string, Amenity[]>();

  for (const rule of RULES) {
    if (seen.has(rule.label)) continue;
    const matched = rule.keywords.some((kw) => matchesKeyword(text, kw));
    if (!matched) continue;

    seen.add(rule.label);
    const list = byCategory.get(rule.category) ?? [];
    list.push({ icon: rule.icon, label: rule.label });
    byCategory.set(rule.category, list);
  }

  return CATEGORY_ORDER.filter((c) => byCategory.has(c)).map((category) => ({
    category,
    items: byCategory.get(category)!,
  }));
}
