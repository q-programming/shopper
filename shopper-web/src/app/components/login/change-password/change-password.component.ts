import {Component, OnInit} from '@angular/core';
import {AbstractControl, FormBuilder, FormGroup, Validators} from "@angular/forms";
import {MyErrorStateMatcher} from "../register/register.component";
import {ActivatedRoute, Router} from "@angular/router";
import {ApiService} from "@services/api.service";
import {AlertService} from "@services/alert.service";
import {environment} from "@env/environment";
import {debounceTime, distinctUntilChanged} from "rxjs/operators";

@Component({
    selector: 'app-change-password',
    templateUrl: './change-password.component.html',
    styles: []
})
export class ChangePasswordComponent implements OnInit {


    passwordForm: FormGroup;
    matcher = new MyErrorStateMatcher();
    currentPass;
    private token: string;


    constructor(private activatedRoute: ActivatedRoute,
                private formBuilder: FormBuilder,
                private router: Router,
                private apiSrv: ApiService,
                private alertSrv: AlertService) {
    }

    ngOnInit() {
        this.activatedRoute.params.subscribe(params => {
            this.token = params['token'];
        });

        this.passwordForm = this.formBuilder.group({
            password: [null, [Validators.required, Validators.minLength(8)]],
            confirmPassword: [null, [Validators.required]]
        }, {validator: this.matchingPasswords});
        this.passwordForm.controls.password.valueChanges.pipe(
            debounceTime(100),
            distinctUntilChanged(),
        ).subscribe(value => {
            this.currentPass = value;
        });
    }

    matchingPasswords(c: AbstractControl): { [key: string]: any } {
        let password = c.get(['password']);
        let confirmPassword = c.get(['confirmPassword']);
        return (password.value !== confirmPassword.value) ? {notSame: true} : null;
    }


    changePassword() {
        this.apiSrv.post(`${environment.auth_url}/password-change`, {
            token: this.token,
            password: this.passwordForm.controls.password.value,
            confirmpassword: this.passwordForm.controls.confirmPassword.value,
        }).subscribe(() => {
            this.alertSrv.success('app.password.change.success');
            this.router.navigate(['/login']);
        }, error => {
            switch (error.error) {
                case 'passwords':
                    this.passwordForm.setErrors({notSame: true});
                    break;
                case 'weak':
                    this.passwordForm.setErrors({weak: true});
                    break;
                case 'expired':
                    this.alertSrv.warning('app.password.change.expired');
                    this.router.navigate(['/login']);
                    break;
            }
        })

    }
}
