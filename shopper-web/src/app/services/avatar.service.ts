import {Injectable} from '@angular/core';
import {Account} from "../model/Account";
import {NGXLogger} from "ngx-logger";
import {ApiService} from "./api.service";
import {environment} from "../../environments/environment";
import {Observable} from "rxjs";

@Injectable()
export class AvatarService {

    constructor(private logger: NGXLogger, private api: ApiService) {
    }

    getUserAvatarById(id: string): Observable<string> {
        return new Observable((observable) => {
            let image = localStorage.getItem("avatar:" + id);
            if (!image) {
                this.logger.debug(`Getting avatar from DB for user ${id}`);
                this.api.getObject(environment.account_url + `/${id}${environment.avatar_url}`).subscribe(result => {
                    if (result) {
                        const dataType = "data:" + result.type + ";base64,";
                        image = dataType + result.image;
                        localStorage.setItem("avatar:" + id, image);
                    } else {
                        image = 'assets/images/avatar-placeholder.png';
                        localStorage.setItem("avatar:" + id, image);
                    }
                    observable.next(image);
                    observable.complete();
                });
            } else {
                this.logger.debug(`Fetching avatar from localStorage for account : ${id}`);
                observable.next(image);
                observable.complete();
            }
        });
    }

    getUserAvatar(account: Account): Observable<string> {
        return this.getUserAvatarById(account.id);

    }

    //TODO Depreciated
    getUserAvatar_old(account: Account) {
        account.avatar = this.getUserAvatarById(account.id);
    }

    updateAvatar(base64Image: String, account: Account) {
        return this.api.post(`${environment.account_url}${environment.avatar_upload_url}`, base64Image).subscribe(() => {
            localStorage.removeItem("avatar:" + account.id);
            this.getUserAvatar(account).subscribe(avatar => {
                account.avatar = avatar;
            });
        })
    }
}
