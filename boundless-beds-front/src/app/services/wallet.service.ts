import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";

@Injectable({
  providedIn: 'root'
})
export class WalletService {

  private apiUrl = '/api/wallets';

  constructor(private http: HttpClient) {
  }

  fundWallet(walletData: any) {
    return this.http.post(this.apiUrl, walletData);
  }
}
