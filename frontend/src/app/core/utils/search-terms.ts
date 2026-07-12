/**
 * Veri seti ülke/şehir adlarını İNGİLİZCE tutuyor (Germany, Netherlands...).
 * Kullanıcı Türkçe yazınca ("almanya", "hollanda") eşleşme olmuyor.
 * Bu sözlük, aramayı göndermeden önce Türkçe terimi veri setindeki karşılığına çevirir.
 */
const TERM_MAP: Record<string, string> = {
  // Ülkeler
  almanya: 'Germany',
  hollanda: 'Netherlands',
  türkiye: 'Turkey',
  turkiye: 'Turkey',
  ispanya: 'Spain',
  italya: 'Italy',
  yunanistan: 'Greece',
  fransa: 'France',
  ingiltere: 'United Kingdom',
  'birleşik krallık': 'United Kingdom',
  'birlesik krallik': 'United Kingdom',
  // Şehir/bölge (veride farklı yazılanlar)
  kapadokya: 'Nevsehir',
  istanbul: 'Istanbul',
  izmir: 'Izmir',
  'i̇stanbul': 'Istanbul',
  'i̇zmir': 'Izmir',
};

/** Arama terimini, biliniyorsa veri setindeki karşılığına çevirir; yoksa aynen döner. */
export function normalizeSearchTerm(raw: string): string {
  const key = raw.trim().toLocaleLowerCase('tr');
  return TERM_MAP[key] ?? raw.trim();
}
