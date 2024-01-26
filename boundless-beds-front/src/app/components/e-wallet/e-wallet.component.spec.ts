import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EWalletComponent } from './e-wallet.component';

describe('EWalletComponent', () => {
  let component: EWalletComponent;
  let fixture: ComponentFixture<EWalletComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EWalletComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(EWalletComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
