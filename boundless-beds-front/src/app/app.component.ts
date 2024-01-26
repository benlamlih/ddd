import {Component} from '@angular/core';
import {RouterOutlet} from '@angular/router';
import {AccountListComponent} from "./components/account-list/account-list.component";
import {AccountCreationComponent} from "./components/account-creation/account-creation.component";
import {HttpClientModule} from "@angular/common/http";
import {AnimationComponent} from "./components/animation/animation.component";
import {NavbarComponent} from "./components/navbar/navbar.component";
import {BookingComponent} from "./components/booking/booking.component";
import {EWalletComponent} from "./components/e-wallet/e-wallet.component";

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, HttpClientModule, AccountListComponent, AccountCreationComponent, AnimationComponent, NavbarComponent, BookingComponent, EWalletComponent],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],
})
export class AppComponent {
  title = 'boundless-beds-front';
}
