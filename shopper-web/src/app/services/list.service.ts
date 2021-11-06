import {Injectable} from '@angular/core';
import {NGXLogger} from "ngx-logger";
import {ApiService} from "./api.service";
import {EMPTY, Observable, Subject} from "rxjs";
import {environment} from "@env/environment";
import {ShoppingList} from "@model/ShoppingList";
import {AvatarService} from "./avatar.service";
import {AuthenticationService} from "./authentication.service";
import {Account} from "@model/Account";
import {ShoppingListDialogComponent} from "../components/dialogs/list/shopping-list-dialog.component";
import {ShareComponent} from "../components/dialogs/share/share.component";
import {AppSettings} from "@model/AppSettings";
import {CategoryPreset} from "@model/CategoryPreset";
import {MatDialog, MatDialogConfig} from "@angular/material/dialog";
import {map} from "rxjs/operators";

@Injectable({
    providedIn: 'root'
})
export class ListService {

    private emitListSource = new Subject<any>();
    listEmiter = this.emitListSource.asObservable();
    listId: number;
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
        this.currentAccount = this.authSrv.currentAccount;
        if (userID) {
            return this.api.getObject<ShoppingList[]>(environment.list_url + `/user/${userID}`, {
                archived: archived,
                items: items
            }).pipe(map(res => this.processList(res)));
        }
        return this.api.getObject<ShoppingList[]>(environment.list_url + '/mine', {
            archived: archived,
            items: items
        }).pipe(map(res => this.processList(res)));
    }

    /**
     * Quickly gets all currently logged in user lists, without items, without avatars etc.
     */
    getMyLists() {
        this.currentAccount = this.authSrv.currentAccount;
        return this.api.getObject<ShoppingList[]>(environment.list_url + '/mine', {
            archived: false,
            items: false,
        }).pipe(map(res => this.processList(res, true)));
    }

    /**
     * Return list with given id
     *
     * @param listID list id
     */
    getListByID(listID: number): Observable<ShoppingList> {
        this.currentAccount = this.authSrv.currentAccount;
        this.listId = listID;
        return this.api.getObject<ShoppingList>(environment.list_url + `/${listID}`).pipe(map(list => {
            list.isOwner = this.isOwner(list);
            this.emitList(list);
            return list;
        }));
    }

    emitList(list: ShoppingList) {
        this.emitListSource.next(list);//tell any other subscriber that there was list loaded
    }

    private processList(lists: ShoppingList[], noAvatars?: boolean): ShoppingList[] {
        lists.forEach(list => {
            list.isOwner = this.isOwner(list);
            if (!list.isOwner && !noAvatars) {
                this.avatarSrv.getUserAvatarById(list.ownerId).subscribe(avatar => list.ownerAvatar = avatar);
            }
        });
        return lists
    }

    /**
     * Checks if currently logged in account is owner of list
     * @param list list for which owner will be checked
     */
    isOwner(list: ShoppingList): boolean {
        return list.ownerId === this.currentAccount.id
    }

    /**
     * Opens new list modal dialog
     */
    openNewListDialog(): Observable<ShoppingList> {
        if (this.dialog.openDialogs.length === 0) {
            return new Observable((observable) => {
                this.loadUserSortingPresets().subscribe(presets => {
                    this.dialogConfig.data = {
                        presets: presets,
                        currentAccount: this.currentAccount
                    };
                    let dialogRef = this.dialog.open(ShoppingListDialogComponent, this.dialogConfig);
                    dialogRef.afterClosed().subscribe(form => {
                        if (form && form.listName) {
                            let list = new ShoppingList();
                            list.name = form.listName;
                            list.preset = form.preset;
                            this.api.postObject<ShoppingList>(environment.list_url + '/add', list).subscribe(newlist => {
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
            });
        } else {
            return EMPTY;
        }
    }

    /**
     * Opens edit list modal dialog
     *
     * @param list Shopping List to be edited
     */
    openEditListDialog(list: ShoppingList): Observable<ShoppingList> {
        if (this.dialog.openDialogs.length === 0) {
            return new Observable((observable) => {
                this.loadUserSortingPresets().subscribe(presets => {
                    this.dialogConfig.data = {
                        presets: presets,
                        list: list,
                        update: true,
                        currentAccount: this.currentAccount
                    };
                    let dialogRef = this.dialog.open(ShoppingListDialogComponent, this.dialogConfig);
                    dialogRef.afterClosed().subscribe(form => {
                        if (form && form.listName) {
                            list.name = form.listName;
                            list.preset = form.preset;
                            this.api.postObject<ShoppingList>(environment.list_url + '/edit', list).subscribe(updated => {
                                if (updated) {
                                    observable.next(updated);
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
            });
        } else {
            return EMPTY
        }
    }

    /**
     * Open share modal dialog for given list
     *
     * @param list list which shared operations are performed
     */
    openShareListDialog(list: ShoppingList): Observable<boolean> {
        if (this.dialog.openDialogs.length === 0) {
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
        } else {
            return EMPTY;
        }
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
     */
    updateList(list: ShoppingList): Observable<ShoppingList> {
        return this.api.post(environment.list_url + `/edit`, list);
    }

    /**
     * Cleanup list by removing all bought items
     *
     * @param listID list id to be cleaned
     */
    cleanup(listID: number) {
        return this.api.postObject<ShoppingList>(environment.list_url + `/${listID}/cleanup`, undefined);
    }

    /**
     * Load all sorting presets for current user
     */
    loadUserSortingPresets(): Observable<CategoryPreset[]> {
        return this.api.getObject<AppSettings>(`${environment.list_url}/presets`);
    }


    /**
     * Save category preset
     * @param preset preset to be updated/saved
     */
    saveCategoryPreset(preset: CategoryPreset): Observable<CategoryPreset> {
        return this.api.postObject(`${environment.list_url}/presets/update`, preset);
    }

    /**
     * Delete category preset. API will determine if user is just leaving that preset or it should be deleted completely
     * @param preset preset to be deleted/left
     */
    deleteCategoryPreset(preset: CategoryPreset): Observable<CategoryPreset> {
        return this.api.postObject(`${environment.list_url}/presets/delete`, preset);
    }

    /**
     * Get default sorting of categories . Determined from api from application property
     */
    getDefaultCategoriesSorting(): Observable<string> {
        return this.api.getObject<AppSettings>(`${environment.config_url}/categories/defaults`);
    }

    copyList(list: ShoppingList): Observable<ShoppingList> {
        return this.api.postObject<ShoppingList>(`${environment.list_url}/${list.id}/copy`, {});
    }
}
