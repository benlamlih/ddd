import { Component } from '@angular/core';
import {AnimationComponent} from "../animation/animation.component";

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [
    AnimationComponent
  ],
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss'],
})
export class HomeComponent {

}
