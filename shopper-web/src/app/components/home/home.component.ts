import {Component, OnInit} from '@angular/core';
import {ApiService} from "../../services/api.service";
import {User} from "../../model/User";
import {AuthenticationService} from "../../services/authentication.service";

@Component({
    selector: 'app-home',
    templateUrl: './home.component.html',
    styles: []
})
export class HomeComponent implements OnInit {

    welcome: String;
    admin: boolean;
    users: User[];

    constructor(private api: ApiService, private authSrv: AuthenticationService) {
    }

    ngOnInit() {
        this.admin = this.authSrv.isAdmin();
        this.getWelcomeMessage();
        this.getUsers();
    }

    getWelcomeMessage() {
        this.api.get("/api/resource").toPromise().then(value => {
            this.welcome = value.content;
        })
    }

    getUsers() {
        if (this.admin) {
            console.log("Hello admin")
            this.api.get("/api/account/all").toPromise().then(res => {
                this.users = res;
            })
        }
    }

}
