import {Component, OnInit} from '@angular/core';
import {environment} from "../../../environments/environment";
import {AuthenticationService} from "../../services/authentication.service";
import {Router} from "@angular/router";

@Component({
    selector: 'app-login',
    templateUrl: 'login.component.html',
    styles: []
})
export class LoginComponent implements OnInit {

    login = environment.context + environment.login_url;

    constructor(private authSrv: AuthenticationService, private router: Router) {
    }

    ngOnInit() {
        if (this.authSrv.currentAccount) {
            this.router.navigate(['/']);
        }
    }

}
