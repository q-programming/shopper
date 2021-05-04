import {Injectable} from '@angular/core';
import {ApiService} from "./api.service";
import {environment} from "@env/environment";
import {Account, Role} from "@model/Account";
import * as _ from 'lodash';
import {AvatarService} from "./avatar.service";
import {TranslateService} from "@ngx-translate/core";
import {AlertService} from "./alert.service";
import {NGXLogger} from "ngx-logger";
import {Observable} from "rxjs";
import {map} from 'rxjs/operators';
import {Product} from "@model/Product";

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
        return this.apiService.get(environment.refresh_token_url, {}).toPromise()
            .then(res => {
                if (res.access_token !== null) {
                    // this.currentAccount = {token: res.access_token};
                    return this.handleLogin();
                }
            })
            .catch((err) => {
                this.alertSrv.error('app.login.error');
                this.logger.error(err);
                sessionStorage.clear();
                return null
            });
    }

    private handleLogin() {
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

    loadFavorites() {
        return this.apiService.getObject<Product[]>(environment.item_url + `/favorites`);
    }

    /**
     * Logouts currently logged user by calling api and setting currentAccount as null
     */
    logout() {
        return this.apiService.post(environment.logout_url, {})
            .pipe(
                map(() => {
                    sessionStorage.clear();
                    this.currentAccount = null;
                })
            );
    }

    /**
     * Return currently logged in account information
     */
    getMyInfo() {
        return this.apiService.post(environment.whoami_url, {},).pipe(map(account => this.currentAccount = account));
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

    login(username: string, password: string): Observable<any> {
        return new Observable((observable) => {
            this.apiService.post(environment.auth_url, {
                username: username,
                password: password
            }, null, false).subscribe((res) => {
                if (res.access_token !== null) {
                    this.getMyInfo().subscribe(user => {
                        this.currentAccount = user as Account;
                        this.avatarSrv.getUserAvatar(this.currentAccount).subscribe(avatar => {
                            this.currentAccount.avatar = avatar;
                            observable.next(this.currentAccount);
                            observable.complete();
                        });
                        // this.avatarSrv.getUserAvatar(this.currentAccount);
                        this.translate.use(this.currentAccount.language);
                    })
                }
            }, err => {
                this.alertSrv.error('app.login.error');
                this.logger.error(err);
                observable.next();
                observable.complete();
            });
        });
    }

    setLanguage() {
        if (this.currentAccount) {
            this.translate.use(this.currentAccount.language);
        } else {
            this.apiService.get(environment.default_lang_url).subscribe(defaults => {
                if (defaults) {
                    let lang = defaults.language;
                    this.translate.setDefaultLang(lang);
                    this.translate.use(lang)
                }
            })
        }
    }
}
