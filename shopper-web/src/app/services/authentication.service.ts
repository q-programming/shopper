import {Injectable} from '@angular/core';
import {ApiService} from "./api.service";
import {environment} from "../../environments/environment";
import {Role, User} from "../model/User";
import * as _ from 'lodash';

@Injectable()
export class AuthenticationService {

    currentUser: User;

    constructor(private apiService: ApiService) {
    }

    initUser() {
        const promise = this.apiService.get(environment.refresh_token_url).toPromise()
            .then(res => {
                if (res.access_token !== null) {
                    return this.getMyInfo().toPromise()
                        .then(user => {
                            this.currentUser = user as User;
                        });
                }
            })
            .catch(() => null);
        return promise;
    }

    logout() {
        return this.apiService.post(environment.logout_url, {})
            .map(() => {
                this.currentUser = null;
            });
    }


    getMyInfo() {
        return this.apiService.get(environment.whoami_url).map(user => this.currentUser = user);
    }

    isAdmin(): boolean {
        if (this.currentUser) {
            return !!_.find(this.currentUser.authorities, (o) => o.authority == Role.ROLE_ADMIN)
        }
        return false;
    }
}
