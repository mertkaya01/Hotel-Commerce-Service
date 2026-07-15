export interface HotelSummary {
  hotelCode: string;
  name: string;
  countryName: string;
  cityName: string;
  rating: string;
}

export interface FacetValue {
  value: string;
  count: number;
}

export interface HotelSearchResponse {
  hotels: HotelSummary[];
  totalResults: number;
  page: number;
  size: number;
  facets: Record<string, FacetValue[]>;
}

export interface HotelDetail {
  id: number;
  hotelCode: string;
  name: string;
  countryName: string;
  cityName: string;
  rating: string;
  address: string | null;
  description: string | null;
  facilities: string | null;
  phoneNumber: string | null;
  websiteUrl: string | null;
  latitude: number | null;
  longitude: number | null;
  photos: string[] | null;
}

export interface Room {
  id: number;
  roomNumber: string;
  roomType: string;
  capacity: number;
  pricePerNight: number;
}

/** Backend'in desteklediği sıralamalar (fiyat demo olduğu için listede yok). */
export type HotelSort = 'relevance' | 'name_asc' | 'name_desc' | 'rating_desc' | 'rating_asc';

export interface HotelSearchParams {
  q?: string;
  country?: string;
  city?: string;
  rating?: string;
  sort?: HotelSort;
  page?: number;
  size?: number;
}
