import { resolveFileUrl } from './file-url';

/**
 * Backend goreceli yol ('/uploads/x.jpg') doner. Gelistirmede frontend 4200,
 * backend 8080'de oldugu icin bu yolun basina backend adresi eklenmeli.
 */
describe('resolveFileUrl', () => {
  it('yuklenen fotografin basina backend adresini ekler', () => {
    // environment.apiUrl gelistirmede http://localhost:8080/api
    expect(resolveFileUrl('/uploads/abc.jpg')).toBe('http://localhost:8080/uploads/abc.jpg');
  });

  it('tam adresli (http) linklere dokunmaz', () => {
    const unsplash = 'https://images.unsplash.com/photo-123?w=800';
    expect(resolveFileUrl(unsplash)).toBe(unsplash);
  });

  it('http:// ile baslayan adrese dokunmaz', () => {
    expect(resolveFileUrl('http://baska-site.com/a.png')).toBe('http://baska-site.com/a.png');
  });

  it('bos deger icin bos string doner', () => {
    expect(resolveFileUrl('')).toBe('');
  });
});
