// Docker/production build'inde kullanilir (angular.json fileReplacements ile).
// API cagrilari ayni origin uzerinden gider; nginx '/api' isteklerini backend'e proxy'ler.
export const environment = {
  production: true,
  apiUrl: '/api',
};
