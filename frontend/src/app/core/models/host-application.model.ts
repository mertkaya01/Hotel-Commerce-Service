export interface HostApplicationRequest {
  firstName: string;
  lastName: string;
  birthDate: string; // yyyy-MM-dd
  description: string;
}

export interface HostApplication {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  birthDate: string;
  description: string;
  status: 'PENDING' | 'APPROVED' | 'REJECTED';
  createdAt: string;
}
