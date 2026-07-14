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

export interface HotelSearchParams {
  q?: string;
  country?: string;
  city?: string;
  rating?: string;
  page?: number;
  size?: number;
}
