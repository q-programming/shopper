import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormControl, Validators} from "@angular/forms";
import {Router} from "@angular/router";
import {ApiService} from "@services/api.service";
import {AlertService} from "@services/alert.service";
import {AuthenticationService} from "@services/authentication.service";
import {environment} from "@env/environment";

@Component({
  selector: 'app-reset-password',
  templateUrl: './reset-password.component.html',
  styles: []
})
export class ResetPasswordComponent implements OnInit {

  emailResetCtrl = new FormControl('', [Validators.required, Validators.email]);

  constructor(private formBuilder: FormBuilder,
              private router: Router,
              private apiSrv: ApiService,
              private alertSrv: AlertService,
              private authSrv: AuthenticationService) {

  }

  ngOnInit() {
    if (this.authSrv.currentAccount) {
      this.router.navigate(['/']);
    }
    this.authSrv.setLanguage();
  }

  reset() {
    this.apiSrv.post(`${environment.auth_url}/password-reset`, this.emailResetCtrl.value).subscribe(() => {
      this.alertSrv.success('app.password.reset.sent');
      this.router.navigate(['/']);
    }, error1 => {
      this.alertSrv.error('app.password.reset.error.mailSrv');
    })
  }

    keyDownFunction(event) {
        if(event.keyCode == 13) {
            alert('you just clicked enter');
            // rest of your code
        }
    }
}
