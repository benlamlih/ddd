import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Account} from '../models/account.model';
import {AccountRequest} from "../models/account-request.model";
import {Reservation} from "../models/reservation.model";

@Injectable({
  providedIn: 'root',
})
export class AccountService {
  private baseUrl = '/api/accounts';
  private headers = new HttpHeaders({'Content-Type': 'application/json'});


  constructor(private http: HttpClient) {
  }

  getAllAccounts(): Observable<Reservation[]> {
    return this.http.get<Reservation[]>("/api/reservations");
  }

  getAccountById(accountId: string | null): Observable<Account> {
    return this.http.get<Account>(this.baseUrl + "/" + accountId);
  }

  getAllAccountReservations(accountId: string | null): Observable<any> {
    return this.http.get<any>("/api/reservations/account/" + accountId);
  }

  createAccount(account: AccountRequest): Observable<any> {
    return this.http.post(this.baseUrl, JSON.stringify(account), {headers: this.headers});
  }
}
