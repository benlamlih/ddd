export interface AccountRequest {
  fullName: string;
  email: string;
  phoneNumber: string;
  balance?: number;
  currency?: string;
}
