import {Component, OnInit} from '@angular/core';
import {ApiService} from "../../services/api.service";
import {Account} from "../../model/Account";
import {AuthenticationService} from "../../services/authentication.service";
import {environment} from "../../../environments/environment";

@Component({
    selector: 'app-home',
    templateUrl: './home.component.html',
    styles: []
})
export class HomeComponent implements OnInit {

    welcome: String;
    admin: boolean;
    users: Account[];

    constructor(private api: ApiService, private authSrv: AuthenticationService) {
    }

    ngOnInit() {
        this.admin = this.authSrv.isAdmin();
        this.getWelcomeMessage();
        this.getUsers();
    }

    getWelcomeMessage() {
        this.api.get(environment.resource_url).toPromise().then(value => {
            this.welcome = value.content;
        })
    }

    getUsers() {
        if (this.admin) {
            this.api.get(environment.all_users_url).toPromise().then(res => {
                this.users = res as Account[];
            })
        }
    }

}
