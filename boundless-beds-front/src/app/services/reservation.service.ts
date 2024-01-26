import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class ReservationService {
  private apiUrl = '/api/reservations';


  constructor(private http: HttpClient) {
  }

  makeReservation(reservationData: any) {
    return this.http.post(this.apiUrl, reservationData);
  }
}
