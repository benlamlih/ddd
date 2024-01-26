import {Component, OnInit, ViewEncapsulation} from '@angular/core';
import {AccountService} from '../../services/account.service';
import {Account} from '../../models/account.model';
import {CommonModule} from "@angular/common";
import {HttpClient} from "@angular/common/http";
import {ActivatedRoute} from "@angular/router";
import {Reservation} from "../../models/reservation.model";
import {AccountRequest} from "../../models/account-request.model";

@Component({
  selector: 'app-account-list',
  templateUrl: './account-list.component.html',
  styleUrls: ['./account-list.component.scss'],
  imports: [CommonModule],
  standalone: true,
  providers: [AccountService],
  encapsulation: ViewEncapsulation.None
})
export class AccountListComponent implements OnInit {
  accountId: string | null = null;
  reservations: Reservation[] | undefined; // Adjust based on your data structure
  balance: number = 0

  constructor(
    private route: ActivatedRoute,
    private accountService: AccountService,
    // very bad...
    private http: HttpClient
  ) {
  }

  getWallet(accountId: string | null) {
    this.accountService.getAccountById(accountId).subscribe(
      (account: Account) => {
        this.balance = account.balance;
        console.log("HAHHAHA : " + this.balance)
      }
    );
  }

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.accountId = params['id']; // Get accountId from route parameters
      console.log("Hello " + this.accountId);
      this.getWallet(this.accountId);
      this.fetchReservations();
    });
  }


  fetchReservations() {
    if (this.accountId) {
      this.getWallet(this.accountId);
      this.accountService.getAllAccounts().subscribe((allReservations: Reservation[]) => {
        // Filter the reservations based on accountId proxy doesn't let me call all the endpoints ???? /* doesn't work ?
        // very slow and bad solution
        // To be fixed one day... Incha'Allah
        // TODO: Found a fix, need to add all the endpoint in the proxy otherwise doesn't work!
        this.reservations = allReservations.filter(reservation => reservation.accountId === this.accountId)
          .sort((a, b) => new Date(a.checkInDate).getTime() - new Date(b.checkInDate).getTime());
      });
    }
  }

  confirmReservation(reservationId: string): void {
    this.http.put(`/api/reservations/${reservationId}/confirm`, {}).subscribe({
      next: (response) => {
        console.log('Reservation confirmed:', response);
        this.fetchReservations();
      },
      error: (error) => console.error('Error confirming reservation:', error)
    });
  }

  cancelReservation(reservationId: string): void {
    this.http.delete(`/api/reservations/${reservationId}`).subscribe({
      next: (response) => {
        console.log('Reservation cancelled:', response);
        this.fetchReservations();
      },
      error: (error) => console.error('Error cancelling reservation:', error)
    });
  }
}
