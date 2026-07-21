/**
 * Veri seti ülke/şehir adlarını İNGİLİZCE tutuyor (Germany, Netherlands...).
 * Kullanıcı bir ülke/şehir adı yazdığında bunu SERBEST METİN olarak aramak yanlış
 * sonuç verir: örn. "almanya" -> "Germany" serbest metni, açıklamasında "Germany"
 * geçen Hollanda otellerini de getirir (Alman sınırına yakın vb.). Bu yüzden bilinen
 * ülke/şehir adlarını FİLTRE (country/city) olarak uyguluyoruz — hem Türkçe hem
 * İngilizce yazımı tanıyarak.
 */

// Türkçe VE İngilizce yazim -> veri setindeki İngilizce ülke adi
const COUNTRY_ALIASES: Record<string, string> = {
  almanya: 'Germany',
  germany: 'Germany',
  hollanda: 'Netherlands',
  netherlands: 'Netherlands',
  türkiye: 'Turkey',
  turkiye: 'Turkey',
  turkey: 'Turkey',
  ispanya: 'Spain',
  spain: 'Spain',
  italya: 'Italy',
  italy: 'Italy',
  yunanistan: 'Greece',
  greece: 'Greece',
  fransa: 'France',
  france: 'France',
  ingiltere: 'United Kingdom',
  'birleşik krallık': 'United Kingdom',
  'birlesik krallik': 'United Kingdom',
  'united kingdom': 'United Kingdom',
  uk: 'United Kingdom',
};

// Veride farklı yazılan / Türkçe şehir adları -> veri setindeki karşılığı
const CITY_ALIASES: Record<string, string> = {
  kapadokya: 'Nevsehir',
  istanbul: 'Istanbul',
  'i̇stanbul': 'Istanbul',
  izmir: 'Izmir',
  'i̇zmir': 'Izmir',
};

export type ResolvedSearch = {
  type: 'country' | 'city' | 'text';
  value: string;
};

/**
 * Arama terimini çözümler: bilinen bir ülke/şehir ise onu FİLTRE olarak
 * (type country/city), değilse serbest metin (type text) olarak döner.
 */
export function resolveSearchTerm(raw: string): ResolvedSearch {
  const key = raw.trim().toLocaleLowerCase('tr');
  if (COUNTRY_ALIASES[key]) return { type: 'country', value: COUNTRY_ALIASES[key] };
  if (CITY_ALIASES[key]) return { type: 'city', value: CITY_ALIASES[key] };
  return { type: 'text', value: raw.trim() };
}
