import {Component, ViewEncapsulation} from '@angular/core';
import {WalletService} from "../../services/wallet.service";
import {FormsModule} from "@angular/forms";
import {NgIf} from "@angular/common";
import {Router} from "@angular/router";


interface WalletResponse {
  balance: number;
}

@Component({
  selector: 'app-e-wallet',
  standalone: true,
  imports: [
    FormsModule,
    NgIf
  ],
  templateUrl: './e-wallet.component.html',
  styleUrls: ['./e-wallet.component.scss'],
  encapsulation: ViewEncapsulation.None
})
export class EWalletComponent {
  walletData = {
    accountId: '',
    amount: null,
    currency: '',
  };

  showModal: boolean = false;
  newBalance: number | null = null;


  constructor(private walletService: WalletService, private router: Router) {
  }

  fundWallet() {
    this.walletService.fundWallet(this.walletData).subscribe(
      response => {
        console.log('Wallet funded successfully', response);
        this.newBalance = (response as WalletResponse).balance;
        this.showModal = true;
      },
      error => {
        console.error('Error funding wallet', error);
      }
    );
  }

  closeModal() {
    this.showModal = false;
    this.router.navigate(['/booking']);
  }
}
