import {Injectable} from '@angular/core';
import {ApiService} from "./api.service";
import {environment} from "../../environments/environment";
import {Account, Role} from "../model/Account";
import * as _ from 'lodash';
import {AvatarService} from "./avatar.service";
import {Cookie} from 'ng2-cookies/ng2-cookies';

@Injectable()
export class AuthenticationService {

    currentAccount: Account;

    constructor(private apiService: ApiService, private avatarSrv: AvatarService) {
    }

    initUser() {
        const promise = this.apiService.get(environment.refresh_token_url).toPromise()
            .then(res => {
                if (res.access_token !== null) {
                    return this.getMyInfo().toPromise()
                        .then(resp => {
                            this.currentAccount = resp as Account;
                            this.avatarSrv.getUserAvatar(this.currentAccount);
                        });
                }
            })
            .catch(() => null);
        return promise;
    }

    logout() {
        return this.apiService.post(environment.logout_url, {})
            .map(() => {
                this.currentAccount = null;
                Cookie.delete('AUTH-TOKEN','/');
                Cookie.delete('AUTH-TOKEN','/shopper');
            });
    }


    getMyInfo() {
        return this.apiService.get(environment.whoami_url).map(account => this.currentAccount = account);
    }

    isAdmin(): boolean {
        if (this.currentAccount) {
            return !!_.find(this.currentAccount.authorities, (o) => o.authority == Role.ROLE_ADMIN)
        }
        return false;
    }
}
