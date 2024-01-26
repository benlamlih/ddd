export interface Reservation {
  id: string;
  accountId: string;
  roomType: string;
  checkInDate: string;
  checkOutDate: string;
  totalPrice: number;
  isConfirmed: boolean;
}
