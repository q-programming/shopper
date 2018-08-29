import {Injectable} from '@angular/core';
import {NGXLogger} from "ngx-logger";
import {ApiService} from "./api.service";
import {Observable} from "rxjs";
import {environment} from "../../environments/environment";
import {ShoppingList} from "../model/ShoppingList";
import {AvatarService} from "./avatar.service";
import {AuthenticationService} from "./authentication.service";
import {Account} from "../model/Account";
import * as _ from 'lodash';

@Injectable({
    providedIn: 'root'
})
export class ListService {

    currentAccount: Account;

    constructor(private logger: NGXLogger, private api: ApiService, private avatarSrv: AvatarService, private authSrv: AuthenticationService) {
        this.currentAccount = this.authSrv.currentAccount
    }


    getUserList(userID: string, items?: boolean): Observable<ShoppingList[]> {
        if (userID) {
            return this.api.getObject<ShoppingList[]>(environment.list_url + `${environment.user_url}${userID}`, {items: items}).map(res => this.processList(res));
        }
        return this.api.getObject<ShoppingList[]>(environment.list_url + environment.mine_url, {items: items}).map(res => this.processList(res))
    }

    getListByID(listID: number): Observable<ShoppingList> {
        return this.api.getObject<ShoppingList>(environment.list_url + `/${listID}`)
    }

    processList(lists: ShoppingList[]): ShoppingList[] {
        let filtered = _.filter(lists, list => this.notOwner(list));
        filtered.forEach(list => {
            list.notOwner = true;
            list.ownerAvatar = this.avatarSrv.getUserAvatarById(list.ownerId)
        });
        return lists
    }

    private notOwner(list: ShoppingList): boolean {
        return list.ownerId !== this.currentAccount.id
    }

}