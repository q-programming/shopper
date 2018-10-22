import {Component, OnInit} from '@angular/core';
import {AuthenticationService} from "@services/authentication.service";


@Component({
    selector: 'app-settings',
    templateUrl: './settings.component.html',
    styles: []
})
export class SettingsComponent implements OnInit {

    admin: boolean;

    constructor(private authSrv: AuthenticationService) {
    }

    ngOnInit() {
        this.admin = this.authSrv.isAdmin();
    }
}

