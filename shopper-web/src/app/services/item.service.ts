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

    constructor(private logger: NGXLogger,
                private api: ApiService,
                private authSrv: AuthenticationService,
                private dialog: MatDialog) {
        this.currentAccount = this.authSrv.currentAccount
    }

    toggleItem(listID: number, item: ListItem): Observable<ListItem> {
        return this.api.postObject<ListItem>(environment.item_url + `/${listID}/toggle`, item)
    }

    updateItem(listID: number, item: ListItem): Observable<ListItem> {
        return this.api.postObject<ListItem>(environment.item_url + `/${listID}/update`, item)
    }

    openNewItemDialog(listID: number): Observable<ShoppingList> {
        const dialogConfig: MatDialogConfig = {
            disableClose: true,
            autoFocus: true,
            width: '500px',
            panelClass: 'shopper-modal',
            data: {
                update: false
            }
        };
        return new Observable((observable) => {
            let dialogRef = this.dialog.open(ItemDialogComponent, dialogConfig);
            dialogRef.afterClosed().subscribe(item => {
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
            });
        });
    }

}