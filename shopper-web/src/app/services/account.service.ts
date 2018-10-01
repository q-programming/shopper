import {Injectable} from '@angular/core';
import {NGXLogger} from "ngx-logger";
import {ApiService} from "./api.service";
import {environment} from "../../environments/environment";
import {Account} from "../model/Account";
import {AvatarService} from "./avatar.service";
import {Observable} from "rxjs";

@Injectable()
export class AccountService {

    constructor(private logger: NGXLogger, private api: ApiService, private avatarSrv: AvatarService) {
    }

    /**
     * Get user with given id
     * @param id id of user to be returned
     */
    getUser(id: String): Promise<Account> {
        return this.api.get(environment.account_url + `/${id}`).toPromise<Account>().then(account => {
            this.avatarSrv.getUserAvatar(account).subscribe(avatar => {
                account.avatar = avatar
            });
            return account
        });
    }

    /**
     * Based on passed Id returns slimed down version of each account with it's avatar
     *
     * @param ids array of ids
     */
    getUsers(ids: string[]): Promise<Account[]> {
        return this.api.postObject<Account[]>(environment.account_url + "/users", ids).toPromise<Account[]>()
            .then(accounts => {
                this.getUsersAvatars(accounts);
                return accounts
            })
    }

    /**
     * Return all users in application
     * TODO probably to be removed
     */
    getAllUsers(): Promise<Account[]> {
        return this.api.get(environment.all_users_url).toPromise<Account[]>().then(accounts => {
            accounts.forEach(account => {
                this.avatarSrv.getUserAvatar(account);
            });
            return accounts
        });
    }

    /**
     * Get avatars for passed account array.
     *
     * @param accounts array of accounts for which avatars are requested
     */
    async getUsersAvatars(accounts: Account[]) {
        accounts.forEach(account => {
            this.avatarSrv.getUserAvatar(account).subscribe(value => {
                account.avatar = value;
            });
        });
    }
}
