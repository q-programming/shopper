import {Component, OnInit} from '@angular/core';
import {AuthenticationService} from "../../services/authentication.service";

@Component({
    selector: 'app-header',
    templateUrl: './header.component.html',
    styles: []
})
export class HeaderComponent implements OnInit {

    constructor(private authSrv: AuthenticationService) {
    }

    ngOnInit() {
    }

    loggedIn() {
        return !!this.authSrv.currentUser;
    }

}
