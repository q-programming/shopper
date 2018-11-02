import {Component, OnInit} from '@angular/core';
import {environment} from "@env/environment";
import {FormBuilder, FormControl, FormGroup, FormGroupDirective, NgForm, Validators} from "@angular/forms";
import {ErrorStateMatcher} from "@angular/material";
import {Router} from "@angular/router";
import {TranslateService} from "@ngx-translate/core";
import {ApiService} from "@services/api.service";
import {AlertService} from "@services/alert.service";

@Component({
    selector: 'app-register',
    templateUrl: './register.component.html',
    styles: []
})
export class RegisterComponent implements OnInit {

    baseForm: FormGroup;
    passwordForm: FormGroup;
    myColors = ['#DD2C00', '#FF6D00', '#FFD600', '#AEEA00', '#00C853'];
    login_url = environment.context + environment.login_url;
    matcher = new MyErrorStateMatcher();
    currentPass;

    constructor(private formBuilder: FormBuilder, private router: Router, private translate: TranslateService, private apiService: ApiService, private alertSrv: AlertService) {
        this.baseForm = this.formBuilder.group({
            name: ['', [Validators.required]],
            surname: ['', [Validators.required]],
            email: ['', [Validators.email, Validators.required]],
        });
        this.passwordForm = this.formBuilder.group({
            password: ['', [Validators.required]],
            confirmPassword: ['']
        }, {validator: this.checkPasswords});
    }

    ngOnInit() {
        this.passwordForm.controls.password.valueChanges
            .debounceTime(100)
            .distinctUntilChanged()
            .subscribe(value => {
                this.currentPass = value;
            });
    }

    checkPasswords(group: FormGroup) {
        let pass = group.controls.password.value;
        let confirmPass = group.controls.confirmPassword.value;
        return pass === confirmPass ? null : {notSame: true}
    }

    register() {
        if (this.baseForm.valid && this.passwordForm.valid) {
            this.apiService.post(`${environment.auth_url}/register`, {
                name: this.baseForm.controls.name.value,
                surname: this.baseForm.controls.surname.value,
                email: this.baseForm.controls.email.value,
                password: this.passwordForm.controls.password.value,
                confirmPassword: this.passwordForm.controls.confirmPassword.value,
            }).subscribe(result => {
                this.alertSrv.successMessage("Successfully registered user, you can now login");
                this.router.navigate(['/login'])
            }, error1 => {
                //TODO handle erros
            })
        }
    }
}

export class MyErrorStateMatcher implements ErrorStateMatcher {
    isErrorState(control: FormControl | null, form: FormGroupDirective | NgForm | null): boolean {
        const invalidCtrl = !!(control && control.invalid && control.parent.dirty);
        const invalidParent = !!(control && control.parent && control.parent.invalid && control.parent.dirty);
        return (invalidCtrl || invalidParent);
    }
}
