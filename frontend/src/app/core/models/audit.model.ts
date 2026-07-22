export interface AuditLog {
  id: number;
  eventType: string;
  actorEmail: string | null;
  description: string | null;
  ipAddress: string | null;
  createdAt: string;
}

export interface AuditLogPage {
  content: AuditLog[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
}
