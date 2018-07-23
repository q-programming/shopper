import {Component, OnInit} from '@angular/core';
import {environment} from "../../../environments/environment";

@Component({
    selector: 'app-login',
    templateUrl: 'login.component.html',
    styles: []
})
export class LoginComponent implements OnInit {

    login = environment.context + environment.login_url;

    constructor() {
    }

    ngOnInit() {
    }

}
