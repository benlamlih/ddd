import { Component } from '@angular/core';
import {AccountCreationComponent} from "../account-creation/account-creation.component";

@Component({
  selector: 'app-account',
  standalone: true,
  imports: [
    AccountCreationComponent
  ],
  templateUrl: './account.component.html',
  styleUrl: './account.component.scss'
})
export class AccountComponent {

}
