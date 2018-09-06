import {Injectable} from "@angular/core";
import {Account} from "../model/Account";
import {NGXLogger} from "ngx-logger";
import {ApiService} from "./api.service";
import {AuthenticationService} from "./authentication.service";
import {MatDialog, MatDialogConfig} from "@angular/material";
import {ListItem} from "../model/ListItem";
import {environment} from "../../environments/environment";
import {Observable} from "rxjs";
import {Product} from "../model/Product";
import {ItemDialogComponent} from "../components/dialogs/item/item-dialog.component";

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

    searchProduct(term: string) {
        return this.api.getObject<Product>(environment.product_url, {term: term})
    }

    openNewItemDialog(): Observable<ListItem> {
        const dialogConfig: MatDialogConfig = {
            disableClose: true,
            autoFocus: true,
            width: '500px',
            panelClass: 'shopper-modal'
        };
        return new Observable((observable) => {
            let dialogRef = this.dialog.open(ItemDialogComponent, dialogConfig);
            dialogRef.afterClosed().subscribe(item => {
                //TODO handl actual object
                // if (listName) {
                //     this.api.postObject<ShoppingList>(environment.list_url + '/add', listName).subscribe(newlist => {
                //         if (newlist) {
                //             observable.next(newlist);
                //             observable.complete()
                //         }
                //     });
                // } else {
                    observable.next(undefined);
                    observable.complete();
                // }
            });
        });
    }

}