import {Component, OnInit} from '@angular/core';
import {environment} from "@env/environment";

@Component({
    selector: 'app-register',
    templateUrl: './register.component.html',
    styles: []
})
export class RegisterComponent implements OnInit {
    login_url = environment.context + environment.login_url;

    constructor() {
    }

    ngOnInit() {
    }

}
