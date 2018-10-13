import {Injectable} from '@angular/core';
import {ApiService} from "./api.service";
import {environment} from "../../environments/environment";
import {Account, Role} from "../model/Account";
import * as _ from 'lodash';
import {AvatarService} from "./avatar.service";
import {TranslateService} from "@ngx-translate/core";
import {AlertService} from "./alert.service";
import {NGXLogger} from "ngx-logger";

@Injectable()
export class AuthenticationService {

    currentAccount: Account;

    constructor(private apiService: ApiService, private avatarSrv: AvatarService, private translate: TranslateService, private alertSrv: AlertService, private logger: NGXLogger) {
    }

    /**
     * Loads initial user
     * First there is call to refresh any potiential tokens (xcors and auth from cookies)
     * If this succeeds , current user is fetched and stored into currentAccount , so that it can be reused across whole application
     */
    initUser() {
        const promise = this.apiService.get(environment.refresh_token_url, {}).toPromise()
            .then(res => {
                if (res.access_token !== null) {
                    // this.currentAccount = {token: res.access_token};
                    return this.getMyInfo().toPromise()
                        .then(resp => {
                            this.currentAccount = resp as Account;
                            this.avatarSrv.getUserAvatar(this.currentAccount).subscribe(avatar => {
                                this.currentAccount.avatar = avatar;
                            });
                            // this.avatarSrv.getUserAvatar(this.currentAccount);
                            this.translate.use(this.currentAccount.language);
                        });
                }
            })
            .catch((err) => {
                this.alertSrv.error('app.login.error');
                this.logger.error(err);
                return null
            });
        return promise;
    }

    /**
     * Logouts currently logged user by calling api and setting currentAccount as null
     */
    logout() {
        return this.apiService.post(environment.logout_url, {})
            .map(() => {
                this.currentAccount = null;
            });
    }

    /**
     * Return currently logged in account information
     */
    getMyInfo() {
        return this.apiService.post(environment.whoami_url, {},).map(account => this.currentAccount = account);
    }

    /**
     * Checks if currently logged in user is administrator
     */
    isAdmin(): boolean {
        if (this.currentAccount) {
            return !!_.find(this.currentAccount.authorities, (o) => o.authority == Role.ROLE_ADMIN)
        }
        return false;
    }
}
