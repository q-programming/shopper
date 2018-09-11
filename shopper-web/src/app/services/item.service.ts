import {Injectable} from "@angular/core";
import {Account} from "../model/Account";
import {NGXLogger} from "ngx-logger";
import {ApiService} from "./api.service";
import {AuthenticationService} from "./authentication.service";
import {MatDialog, MatDialogConfig} from "@angular/material";
import {ListItem} from "../model/ListItem";
import {environment} from "../../environments/environment";
import {Observable} from "rxjs";
import {ItemDialogComponent} from "../components/dialogs/item/item-dialog.component";
import {ShoppingList} from "../model/ShoppingList";

@Injectable({
    providedIn: 'root'
})
export class ItemService {

    currentAccount: Account;
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
                private dialog: MatDialog) {
        this.currentAccount = this.authSrv.currentAccount
    }

    toggleItem(listID: number, item: ListItem): Observable<ListItem> {
        return this.api.postObject<ListItem>(environment.item_url + `/${listID}/toggle`, item)
    }

    updateItem(listID: number, item: ListItem): Observable<ShoppingList> {
        return this.api.postObject<ShoppingList>(environment.item_url + `/${listID}/update`, item)
    }
    deleteItem(listID: number, item: ListItem): Observable<ShoppingList> {
        return this.api.postObject<ShoppingList>(environment.item_url + `/${listID}/delete`, item)
    }

    openNewItemDialog(listID: number): Observable<ShoppingList> {
        this.dialogConfig.data.update = false;
        this.dialogConfig.data.item = undefined;
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
                }
            });
        });
    }

    openEditItemDialog(listID: number, item: ListItem): Observable<ShoppingList> {
        this.dialogConfig.data.update = true;
        this.dialogConfig.data.item = item;
        return new Observable((observable) => {
            let dialogRef = this.dialog.open(ItemDialogComponent, this.dialogConfig);
            dialogRef.afterClosed().subscribe(item => {
                if (item) {
                    this.api.postObject<ListItem>(environment.item_url + `/${listID}/update`, item).subscribe(edited => {
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

}