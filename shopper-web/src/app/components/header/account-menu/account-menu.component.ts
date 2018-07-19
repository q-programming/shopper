import {Component, OnInit} from '@angular/core';
import {Router} from "@angular/router";
import {AuthenticationService} from "../../../services/authentication.service";
import {User} from "../../../model/User";

@Component({
    selector: 'app-account-menu',
    templateUrl: './account-menu.component.html',
    styles: []
})
export class AccountMenuComponent implements OnInit {

    user: User;

    constructor(
        private router: Router,
        private authSrv: AuthenticationService
    ) {
    }

    ngOnInit() {
        this.user = this.authSrv.currentUser;
    }

    logout() {
        this.authSrv.logout().subscribe(() => {
            this.router.navigate(['/login']);
        });
    }

}
