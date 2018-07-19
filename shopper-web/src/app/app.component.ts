import {Component} from '@angular/core';
import {Spinkit} from "ng-http-loader/spinkits";
import {Router} from "@angular/router";
import {HttpClient} from "@angular/common/http";
import {AuthenticationService} from "./services/authentication.service";

@Component({
    selector: 'app-root',
    templateUrl: 'app.component.html',
    styles: []
})
export class AppComponent {
    public spinkit = Spinkit;

    constructor(private http: HttpClient, private router: Router, private authService: AuthenticationService) {
    }

    logout() {
        this.authService.logout();
    }
}

