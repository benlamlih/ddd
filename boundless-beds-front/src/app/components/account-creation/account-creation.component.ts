import {Component, ViewEncapsulation} from '@angular/core';
import {AccountService} from "../../services/account.service";
import {AccountRequest} from "../../models/account-request.model";
import {CommonModule} from "@angular/common";
import {FormsModule} from "@angular/forms";
import {Router, RouterLink} from "@angular/router";
import {CubeComponent} from "../cube/cube.component";

@Component({
  selector: 'app-account-creation',
  standalone: true,
  imports: [CommonModule, FormsModule, CubeComponent, RouterLink],
  templateUrl: './account-creation.component.html',
  styleUrls: ['./account-creation.component.scss'],
  encapsulation: ViewEncapsulation.None
})
export class AccountCreationComponent {
  account: AccountRequest = {
    fullName: '',
    email: '',
    phoneNumber: '',
    balance: 0,
    currency: "EUR"
  };

  createdAccountId: string | null = null;
  errorMessage: string | null = null;
  showSuccessModal: boolean = false;
  showAccessModal: boolean = false;
  loginAccountId: string = '';


  constructor(private accountService: AccountService, private router: Router) {
  }

  createAccount() {
    this.accountService.createAccount(this.account).subscribe({
      next: (response) => {
        this.createdAccountId = response.id;
        this.showSuccessModal = true;
        this.errorMessage = null;
        console.log('Account created:', response);
      },
      error: (error) => {
        this.showSuccessModal = false;
        if (error.error instanceof ErrorEvent) {
          this.errorMessage = 'An error occurred: ' + error.error.message;
        } else {
          this.errorMessage = error.error.message || 'Server error';
        }
        console.error('Error creating account:', error);
      }
    });
  }

  closeModal() {
    this.createdAccountId = null;
    this.router.navigate(['/e-wallet']);
  }

  closeAccessModal() {
    this.showAccessModal = false;
  }

  redirectToAccounts() {
    if (this.loginAccountId) {
      this.router.navigate(['/reservations', this.loginAccountId]);
      this.closeAccessModal();
    }
  }
}
