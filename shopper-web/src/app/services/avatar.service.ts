import {Injectable} from '@angular/core';
import {Account} from "../model/Account";
import {NGXLogger} from "ngx-logger";
import {ApiService} from "./api.service";
import {environment} from "../../environments/environment";

@Injectable()
export class AvatarService {

    constructor(private logger: NGXLogger, private api: ApiService) {
    }

    getUserAvatar(account: Account) {
        let image = localStorage.getItem("avatar:" + account.id);
        if (!image) {
            this.logger.debug(`Getting avatar from DB for user ${account.id}`);
            let res = this.api.getObject(environment.account_url + `/${account.id}${environment.avatar_url}`).subscribe(result => {
                if (result) {
                    const dataType = "data:" + result.type + ";base64,";
                    image = dataType + result.image;
                    localStorage.setItem("avatar:" + account.id, image);
                    account.avatar = image;
                } else {
                    account.avatar = 'assets/images/avatar-placeholder.png';
                    localStorage.setItem("avatar:" + account.id, account.avatar);
                }
            });
            this.logger.info(res);
        } else {
            this.logger.debug(`Fetching avatar from localStorage for account : ${account.id}`);
            account.avatar = image;
        }
    }

    updateAvatar(base64Image: String, account: Account) {
        return this.api.post(`${environment.account_url}${environment.avatar_upload_url}`, base64Image).subscribe(() => {
            localStorage.removeItem("avatar:" + account.id);
            this.getUserAvatar(account);
        })
    }
}
