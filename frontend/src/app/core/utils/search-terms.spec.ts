import { resolveSearchTerm } from './search-terms';

describe('resolveSearchTerm', () => {
  it('Türkçe ülke adını country filtresine çevirir', () => {
    expect(resolveSearchTerm('almanya')).toEqual({ type: 'country', value: 'Germany' });
    expect(resolveSearchTerm('hollanda')).toEqual({ type: 'country', value: 'Netherlands' });
  });

  it('İngilizce ülke adını da tanır', () => {
    expect(resolveSearchTerm('Germany')).toEqual({ type: 'country', value: 'Germany' });
    expect(resolveSearchTerm('  NETHERLANDS ')).toEqual({ type: 'country', value: 'Netherlands' });
  });

  it('bilinen şehri city filtresine çevirir', () => {
    expect(resolveSearchTerm('kapadokya')).toEqual({ type: 'city', value: 'Nevsehir' });
    expect(resolveSearchTerm('istanbul')).toEqual({ type: 'city', value: 'Istanbul' });
  });

  it('bilinmeyen terimi serbest metin olarak bırakır', () => {
    expect(resolveSearchTerm('Hilton')).toEqual({ type: 'text', value: 'Hilton' });
    expect(resolveSearchTerm('  bodrum ')).toEqual({ type: 'text', value: 'bodrum' });
  });
});
