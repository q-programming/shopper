import {Injectable} from "@angular/core";
import {Account} from "@model/Account";
import {NGXLogger} from "ngx-logger";
import {ApiService} from "./api.service";
import {AuthenticationService} from "./authentication.service";
import {ListItem} from "@model/ListItem";
import {environment} from "@env/environment";
import {Observable} from "rxjs";
import {ItemDialogComponent} from "../components/dialogs/item/item-dialog.component";
import {ShoppingList} from "@model/ShoppingList";
import {CategoryOption} from "@model/CategoryOption";
import {Category} from "@model/Category";
import {TranslateService} from "@ngx-translate/core";
import {Product} from "@model/Product";
import {MatDialog, MatDialogConfig} from "@angular/material/dialog";

@Injectable({
    providedIn: 'root'
})
export class ItemService {

    currentAccount: Account;
    categories: CategoryOption[] = [];
    account_favorites: string[] = [];
    private dialogConfig: MatDialogConfig = {
        disableClose: true,
        autoFocus: true,
        width: '500px',
        panelClass: 'shopper-modal',
        data: {
            update: false
        }
    };

    constructor(private logger: NGXLogger,
                private api: ApiService,
                private authSrv: AuthenticationService,
                private translate: TranslateService,
                private dialog: MatDialog) {
        this.currentAccount = this.authSrv.currentAccount;
        this.loadCategoriesWithLocalName();
        this.loadAccountFavorites();
    }

    /**
     * Togle item done status ( bought or to buy )
     *
     * @param listID items list id
     * @param item item to be toggled
     */
    toggleItem(listID: number, item: ListItem): Observable<ListItem> {
        return this.api.postObject<ListItem>(environment.item_url + `/${listID}/toggle`, item)
    }

    /**
     * set item done status ( bought or to buy )
     *
     * @param listID items list id
     * @param item item to be toggled
     */
    setItemDone(listID: number, item: ListItem): Observable<ListItem> {
        return this.api.patch(environment.item_url + `/${listID}/${item.id}/done`, !item.done)
    }

    /**
     * Updates item with new data
     *
     * @param listID items list id
     * @param item new item data
     */
    updateItem(listID: number, item: ListItem): Observable<ShoppingList> {
        return this.api.postObject<ShoppingList>(environment.item_url + `/${listID}/update`, item)
    }

    /**
     * Deletes item from list
     *
     * @param listID items list id
     * @param item new item data
     */
    deleteItem(listID: number, item: ListItem): Observable<ShoppingList> {
        return this.api.postObject<ShoppingList>(environment.item_url + `/${listID}/delete`, item)
    }

    /**
     * Open modal dialog to add new item to list
     *
     * @param listID list for which item will be added
     */
    openNewItemDialog(listID: number): Observable<ShoppingList> {
        this.dialogConfig.data.update = false;
        this.dialogConfig.data.item = undefined;
        this.dialogConfig.data.categories = this.categories;
        this.dialogConfig.data.favorites = this.favorites();
        this.dialogConfig.data.listID = listID;
        return new Observable((observable) => {
            let dialogRef = this.dialog.open(ItemDialogComponent, this.dialogConfig);
            dialogRef.afterClosed().subscribe(item => {
                if (item) {
                    this.api.postObject<ShoppingList>(environment.item_url + `/${listID}/add`, item).subscribe(list => {
                        if (list) {
                            observable.next(list);
                            observable.complete();
                        }
                    }, error => {
                        this.logger.error(error);
                        observable.next(undefined);
                        observable.complete();
                    });
                } else {
                    observable.next(undefined);
                    observable.complete();
                }
            });
        });
    }

    /**
     * Open edit modal dialog
     *
     * @param listID listID list where item will be edited
     * @param item item to be updated
     */
    openEditItemDialog(listID: number, item: ListItem): Observable<ShoppingList> {
        this.dialogConfig.data.update = true;
        this.dialogConfig.data.item = item;
        this.dialogConfig.data.categories = this.categories;
        this.dialogConfig.data.favorites = this.favorites();
        this.dialogConfig.data.listID = listID;
        return new Observable((observable) => {
            let dialogRef = this.dialog.open(ItemDialogComponent, this.dialogConfig);
            dialogRef.afterClosed().subscribe(item => {
                if (item) {
                    this.updateItem(listID, item).subscribe(edited => {
                        if (edited) {
                            observable.next(edited);
                            observable.complete();
                        }
                    }, error => {
                        this.logger.error(error);
                        observable.next(undefined);
                        observable.complete();
                    });
                }
            });
        });
    }

    private favorites(): string[] {
        if (!this.account_favorites || !this.account_favorites.length) {
            this.loadAccountFavorites();
        }
        return this.account_favorites;
    }

    private loadAccountFavorites() {
        if (!this.account_favorites || !this.account_favorites.length) {
            const favJSON = sessionStorage.getItem('favorites');
            if (favJSON) {
                this.account_favorites = (JSON.parse(favJSON) as Product[]).map(product => product.name);
            } else {
                this.authSrv.loadFavorites().subscribe(response => {
                    this.account_favorites = response.map(product => product.name);
                    sessionStorage.setItem('favorites', JSON.stringify(response));
                });
            }
        }
    }


    /**
     * Create new item for list
     *
     * @param listID new items lits id
     * @param item new item data
     */
    createNewItem(listID: number, item: ListItem): Observable<ShoppingList> {
        return this.api.postObject<ShoppingList>(environment.item_url + `/${listID}/add`, item)
    }

    /**
     * Load all categories with localised name and sort by name
     */
    loadCategoriesWithLocalName() {
        this.categories = [];
        Object.values(Category).map(value => {
            return this.translate.get(value.toString()).subscribe(name => {
                this.categories.push({
                    category: value,
                    name: name
                });
            }, undefined, () => {
                this.categories.sort((a, b) => a.name.localeCompare(b.name))
            })
        });
    }


}