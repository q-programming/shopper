import {Injectable} from '@angular/core';
import {NGXLogger} from "ngx-logger";
import {ApiService} from "./api.service";
import {environment} from "@env/environment";
import {Account} from "@model/Account";
import {AvatarService} from "./avatar.service";

@Injectable()
export class AccountService {

    constructor(private logger: NGXLogger, private api: ApiService, private avatarSrv: AvatarService) {
    }

    /**
     * Based on passed Id returns slimed down version of each account with it's avatar
     *
     * @param ids array of ids
     */
    getUsers(ids: string[]): Promise<Account[]> {
        return this.api.postObject<Account[]>(environment.account_url + "/users", ids).toPromise()
            .then(accounts => {
                this.getUsersAvatars(accounts);
                return accounts
            })
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
