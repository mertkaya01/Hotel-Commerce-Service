import { environment } from '../../../environments/environment';

/**
 * Backend, yuklenen dosyalar icin '/uploads/xxx.jpg' gibi GORECELI bir yol doner.
 * Gelistirmede frontend 4200, backend 8080'de oldugu icin bu yolun basina backend
 * adresini eklememiz gerekir. Production'da apiUrl '/api' (ayni origin) oldugundan
 * yol oldugu gibi kullanilir.
 */
export function resolveFileUrl(url: string): string {
  if (!url) return '';
  // Zaten tam adres (orn. demo galerideki Unsplash linkleri) -> dokunma
  if (url.startsWith('http://') || url.startsWith('https://')) return url;

  const origin = environment.apiUrl.replace(/\/api\/?$/, '');
  return `${origin}${url}`;
}
