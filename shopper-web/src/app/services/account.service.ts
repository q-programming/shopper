import {Injectable} from '@angular/core';
import {NGXLogger} from "ngx-logger";
import {ApiService} from "./api.service";
import {environment} from "../../environments/environment";
import {Account} from "../model/Account";
import {AvatarService} from "./avatar.service";

@Injectable()
export class AccountService {

    constructor(private logger: NGXLogger, private api: ApiService, private avatarSrv: AvatarService) {
    }


    getUser(id: String): Promise<Account> {
        return this.api.get(environment.account_url + `/${id}`).toPromise<Account>().then(account => {
            this.avatarSrv.getUserAvatar(account);
            return account
        });
    }

    getAllUsers(): Promise<Account[]> {
        return this.api.get(environment.all_users_url).toPromise<Account[]>().then(accounts => {
            accounts.forEach(account => {
                this.avatarSrv.getUserAvatar(account);
            });
            return accounts
        });
    }
}
