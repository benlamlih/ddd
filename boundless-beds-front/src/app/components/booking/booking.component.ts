import {Component, ViewEncapsulation} from '@angular/core';
import {ReservationService} from "../../services/reservation.service";
import {FormsModule} from "@angular/forms";
import {NgClass, NgIf} from "@angular/common";
import {Router} from "@angular/router";

@Component({
  selector: 'app-booking',
  standalone: true,
  imports: [
    FormsModule,
    NgClass,
    NgIf
  ],
  templateUrl: './booking.component.html',
  styleUrls: ['./booking.component.scss'],
  encapsulation: ViewEncapsulation.None
})
export class BookingComponent {
  reservationData = {
    accountId: '',
    roomType: '',
    checkInDate: '',
    checkOutDate: ''
  };
  roomSelected = false;
  selectedRoom: string = '';
  showSuccessModal: boolean = false;
  showErrorModal: boolean = false;


  constructor(private reservationService: ReservationService, private router: Router) {
  }

  closeModal() {
    this.showSuccessModal = false;
    this.showErrorModal = false;
  }

  selectRoomType(roomType: string) {
    this.reservationData.roomType = roomType;
    this.roomSelected = true; // Room is now selected
    this.selectedRoom = roomType;
  }

  onSubmit() {
    if (!this.roomSelected) {
      console.error('Please select a room type before booking.');
      return;
    }
    this.reservationService.makeReservation(this.reservationData).subscribe(
      response => {
        console.log('Success!', response);
        this.showSuccessModal = true;
      },
      error => {
        console.error('Error!', error);
        this.showErrorModal = true;
      }
    );
  }

  goToReservations() {
    if (this.reservationData.accountId) {
      this.router.navigate(['/reservations', this.reservationData.accountId]);
    }
    this.closeModal();
  }

}
