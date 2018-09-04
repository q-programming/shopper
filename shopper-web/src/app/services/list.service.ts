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
import {MatDialog, MatDialogConfig} from "@angular/material";
import {NewShoppingListComponent} from "../components/dialogs/new-list/new-shoppinglist.component";

@Injectable({
    providedIn: 'root'
})
export class ListService {

    currentAccount: Account;

    constructor(private logger: NGXLogger,
                private api: ApiService,
                private avatarSrv: AvatarService,
                private authSrv: AuthenticationService,
                private dialog: MatDialog) {
        this.currentAccount = this.authSrv.currentAccount
    }


    getUserList(userID?: string, items?: boolean, archived?: boolean): Observable<ShoppingList[]> {
        if (userID) {
            return this.api.getObject<ShoppingList[]>(environment.list_url + `${environment.user_url}${userID}`, {
                archived: archived,
                items: items
            }).map(res => this.processList(res));
        }
        return this.api.getObject<ShoppingList[]>(environment.list_url + environment.mine_url, {
            archived: archived,
            items: items
        }).map(res => this.processList(res))
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

    createList(listName: string): Observable<ShoppingList> {
        return this.api.postObject<ShoppingList>(environment.list_url + '/add', listName)
    }

    private notOwner(list: ShoppingList): boolean {
        return list.ownerId !== this.currentAccount.id
    }

    openNewListDialog(): Observable<ShoppingList> {
        const dialogConfig: MatDialogConfig = {
            disableClose: true,
            autoFocus: true,
            width: '500px',
            panelClass: 'shopper-modal'
        };
        return new Observable((observable) => {
            let dialogRef = this.dialog.open(NewShoppingListComponent, dialogConfig);
            dialogRef.afterClosed().subscribe(listName => {
                if (listName) {
                    this.api.postObject<ShoppingList>(environment.list_url + '/add', listName).subscribe(newlist => {
                        if (newlist) {
                            observable.next(newlist);
                            observable.complete()
                        }
                    });
                } else {
                    observable.next(undefined);
                    observable.complete();
                }
            });
        });
    }

    archive(list: ShoppingList): Observable<ShoppingList> {
        return this.api.post(environment.list_url + `/${list.id}/archive`, null)
    }

    delete(list: ShoppingList): Observable<any> {
        return this.api.post(environment.list_url + `/${list.id}/delete`, null)
    }
}
