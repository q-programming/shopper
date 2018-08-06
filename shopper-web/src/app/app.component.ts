import {Component, OnInit, ViewChild} from '@angular/core';
import {Router} from "@angular/router";
import {HttpClient} from "@angular/common/http";
import {AuthenticationService} from "./services/authentication.service";
import {Account} from "./model/Account";
import {MatSidenav} from "@angular/material";
import {LoaderComponent} from "./components/loader/loader.component";

@Component({
    selector: 'app-root',
    templateUrl: 'app.component.html',
    styles: []
})
export class AppComponent implements OnInit {

    account: Account;
    message_count = {count: ""};
    lists = [1, 2, 3, 4];
    public loader = LoaderComponent;
    @ViewChild("sidenav")
    private sidenav: MatSidenav;

    constructor(private http: HttpClient, private router: Router, private authSrv: AuthenticationService) {
        router.events.subscribe(() => {
            if (this.sidenav.opened) {
                this.sidenav.close();
            }
        })
    }

    ngOnInit() {
        this.account = this.authSrv.currentAccount;
        this.message_count.count = "" + 1;
    }

    loggedIn() {
        return !!this.authSrv.currentAccount;
    }

    logout() {
        this.authSrv.logout().subscribe(() => {
            this.router.navigate(['/login']);
        });
    }
}

