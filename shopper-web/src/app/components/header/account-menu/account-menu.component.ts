import {Component, OnInit, ViewChild} from '@angular/core';
import {Router} from "@angular/router";
import {AuthenticationService} from "../../../services/authentication.service";
import {Account} from "../../../model/Account";
import {ModalDirective} from "angular-bootstrap-md";

@Component({
    selector: 'app-account-menu',
    templateUrl: './account-menu.component.html',
    styles: []
})
export class AccountMenuComponent implements OnInit {

    user: Account;
    @ViewChild('userMenuActivate')
    userMenuActivate: ModalDirective;
    message_count = {count: ""};


    constructor(
        private router: Router,
        private authSrv: AuthenticationService
    ) {
    }

    ngOnInit() {
        this.userMenuActivate.config = {backdrop: false};
        this.user = this.authSrv.currentAccount;
        this.message_count.count = ""+1;
    }

    logout() {
        this.authSrv.logout().subscribe(() => {
            this.router.navigate(['/login']);
        });
    }

}
