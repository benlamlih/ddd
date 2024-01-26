import {Routes} from '@angular/router';
import {BookingComponent} from "./components/booking/booking.component";
import {AccountComponent} from "./components/account/account.component";
import {EWalletComponent} from "./components/e-wallet/e-wallet.component";
import {AccountCreationComponent} from "./components/account-creation/account-creation.component";
import {HomeComponent} from "./components/home/home.component";
import {AccountListComponent} from "./components/account-list/account-list.component";

export const routes: Routes = [
  {path: 'home', component: HomeComponent},
  {path: '', redirectTo: '/home', pathMatch: 'full'},
  {path: 'booking', component: BookingComponent},
  {path: 'account', component: AccountComponent},
  {path: 'e-wallet', component: EWalletComponent},
  {path: 'account-res', component: AccountListComponent},
  {path: 'reservations/:id', component: AccountListComponent},
];
