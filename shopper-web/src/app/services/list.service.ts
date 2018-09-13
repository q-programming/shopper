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
import {NewShoppingListDialogComponent} from "../components/dialogs/new-list/new-shopping-list-dialog.component";
import {ShareComponent} from "../components/dialogs/share/share.component";

@Injectable({
    providedIn: 'root'
})
export class ListService {

    currentAccount: Account;
    dialogConfig: MatDialogConfig = {
        disableClose: true,
        autoFocus: true,
        width: '500px',
        panelClass: 'shopper-modal'
    };


    constructor(private logger: NGXLogger,
                private api: ApiService,
                private avatarSrv: AvatarService,
                private authSrv: AuthenticationService,
                private dialog: MatDialog) {
        this.currentAccount = this.authSrv.currentAccount
    }


    getUserList(userID?: string, items?: boolean, archived?: boolean): Observable<ShoppingList[]> {
        if (userID) {
            return this.api.getObject<ShoppingList[]>(environment.list_url + `/user/${userID}`, {
                archived: archived,
                items: items
            }).map(res => this.processList(res));
        }
        return this.api.getObject<ShoppingList[]>(environment.list_url + '/mine', {
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

    private notOwner(list: ShoppingList): boolean {
        return list.ownerId !== this.currentAccount.id
    }

    openNewListDialog(): Observable<ShoppingList> {
        return new Observable((observable) => {
            let dialogRef = this.dialog.open(NewShoppingListDialogComponent, this.dialogConfig);
            dialogRef.afterClosed().subscribe(listName => {
                if (listName) {
                    this.api.postObject<ShoppingList>(environment.list_url + '/add', listName).subscribe(newlist => {
                        if (newlist) {
                            observable.next(newlist);
                            observable.complete()
                        }
                    });
                }
            }, error => {
                this.logger.error(error);
                observable.next(undefined);
                observable.complete();
            });
        });
    }

    openShareListDialog(list: ShoppingList): Observable<string> {
        this.dialogConfig.data = list;
        return new Observable((observable) => {
            let dialogRef = this.dialog.open(ShareComponent, this.dialogConfig);
            dialogRef.afterClosed().subscribe(email => {
                this.dialogConfig.data = undefined;
                if (email) {
                    this.api.postObject<ShoppingList>(environment.list_url + `/${list.id}/share`, email).subscribe(shared => {
                        if (shared) {
                            observable.next(email);
                            observable.complete()
                        }
                    });
                }
            }, error => {
                this.dialogConfig.data = undefined;
                this.logger.error(error);
                observable.next(undefined);
                observable.complete();
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
