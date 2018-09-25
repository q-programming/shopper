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
import {ShoppingListDialogComponent} from "../components/dialogs/list/shopping-list-dialog.component";
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

    /**
     * Returns user lists
     *
     * @param userID (optional) user id, if not passed currently logged in user list will be fetched
     * @param items (optional) should list be returned with items
     * @param archived (optional) returned lists archived or not
     */
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

    /**
     * Return list with given id
     *
     * @param listID list id
     */
    getListByID(listID: number): Observable<ShoppingList> {
        return this.api.getObject<ShoppingList>(environment.list_url + `/${listID}`)
    }

    private processList(lists: ShoppingList[]): ShoppingList[] {
        let filtered = _.filter(lists, list => this.notOwner(list));
        filtered.forEach(list => {
            list.notOwner = true;
            this.avatarSrv.getUserAvatarById(list.ownerId).subscribe(avatar => list.ownerAvatar = avatar);
        });
        return lists
    }

    private notOwner(list: ShoppingList): boolean {
        return list.ownerId !== this.currentAccount.id
    }

    /**
     * Opens new list modal dialog
     */
    openNewListDialog(): Observable<ShoppingList> {
        return new Observable((observable) => {
            let dialogRef = this.dialog.open(ShoppingListDialogComponent, this.dialogConfig);
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

    /**
     * Open share modal dialog for given list
     *
     * @param list list which shared operations are performed
     */
    openShareListDialog(list: ShoppingList): Observable<boolean> {
        this.dialogConfig.data = list;
        return new Observable((observable) => {
            let dialogRef = this.dialog.open(ShareComponent, this.dialogConfig);
            dialogRef.afterClosed().subscribe(done => {
                this.dialogConfig.data = undefined;
                if (done) {
                    observable.next(done);
                    observable.complete()
                }
            }, error => {
                this.dialogConfig.data = undefined;
                this.logger.error(error);
                observable.next(undefined);
                observable.complete();
            });
        });
    }

    /**
     * Archive list
     *
     * @param list list to be archived
     */
    archive(list: ShoppingList): Observable<ShoppingList> {
        return this.api.post(environment.list_url + `/${list.id}/archive`, null);
    }

    /**
     * Delete list
     *
     * @param list list to be deleted
     */
    delete(list: ShoppingList): Observable<any> {
        return this.api.post(environment.list_url + `/${list.id}/delete`, null);
    }

    /**
     * Edit list name
     *
     * @param list list to be edited
     * @param listName new list name
     */
    editName(list: ShoppingList, listName: string): Observable<ShoppingList> {
        return this.api.post(environment.list_url + `/${list.id}/edit`, listName);
    }

    /**
     * Cleanup list by removing all bought items
     *
     * @param listID list id to be cleaned
     */
    cleanup(listID: number) {
        return this.api.postObject<ShoppingList>(environment.list_url + `/${listID}/cleanup`, undefined);
    }

}
