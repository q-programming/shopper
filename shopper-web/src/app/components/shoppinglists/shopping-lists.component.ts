import {Component, OnInit} from '@angular/core';
import {ListService} from "../../services/list.service";
import {ActivatedRoute} from "@angular/router";
import {ShoppingList} from "../../model/ShoppingList";
import {AuthenticationService} from "../../services/authentication.service";
import {Account} from "../../model/Account";
import {MatDialog, MatDialogConfig} from "@angular/material";
import {NewShoppingListComponent} from "./new-shoppinglist.component";
import {AlertService} from "../../services/alert.service";


@Component({
    selector: 'app-list',
    templateUrl: './shopping-lists.component.html',
    styleUrls: ['./shopping-lists.component.css']
})
export class ShoppingListsComponent implements OnInit {
    userID: string;
    lists: ShoppingList[];
    account: Account;

    constructor(private listSrv: ListService, private route: ActivatedRoute, private authSrv: AuthenticationService, public dialog: MatDialog, private alertSrv: AlertService) {
    }

    ngOnInit() {
        this.account = this.authSrv.currentAccount;
        this.route.params.subscribe(params => {
            this.userID = params['userid'];
            this.loadUserLists();
        });
    }

    private loadUserLists() {
        this.listSrv.getUserList(this.userID, true).subscribe(lists => {
            this.lists = lists;
        })
    }

    openDialog() {
        const dialogConfig = new MatDialogConfig();
        dialogConfig.disableClose = true;
        dialogConfig.autoFocus = true;
        const dialogRef = this.dialog.open(NewShoppingListComponent, dialogConfig);
        dialogRef.afterClosed().subscribe(listName => {
            this.listSrv.createList(listName).subscribe(() => {
                //TODO figure out why double alert ?
                // this.alertSrv.success("app.shopping.create.success");
                this.loadUserLists();
            });
        });
    }
}
