export interface RoomInput {
  roomType: string; // SINGLE / DOUBLE / SUITE / DELUXE
  capacity: number;
  pricePerNight: number;
}

export interface UploadResponse {
  url: string; // orn. /uploads/ab12....jpg
}

export interface HotelListingRequest {
  name: string;
  countryName: string;
  cityName: string;
  rating: string;
  address: string;
  description: string;
  amenities: string[]; // olanak anahtarları (wifi, pool, ...)
  photos: string[]; // fotoğraf URL'leri
  rooms: RoomInput[];
}

export interface HotelListing {
  id: number;
  hotelCode: string;
  name: string;
  cityName: string;
  countryName: string;
  rating: string;
  status: 'PENDING' | 'APPROVED' | 'REJECTED';
  ownerName: string | null;
  photos: string[];
  roomCount: number;
}
