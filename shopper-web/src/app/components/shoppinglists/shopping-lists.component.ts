import {Component, OnDestroy, OnInit} from '@angular/core';
import {ListService} from "@services/list.service";
import {MenuAction, MenuActionsService} from "@services/menu-actions.service";
import {AuthenticationService} from "@services/authentication.service";
import {AlertService} from "@services/alert.service";
import {ActivatedRoute, Router} from "@angular/router";
import {ShoppingList} from "@model/ShoppingList";
import {Account} from "@model/Account";
import {NGXLogger} from "ngx-logger";
import {ConfirmDialog, ConfirmDialogComponent} from "../dialogs/confirm/confirm-dialog.component";
import {Subscription} from "rxjs";
import {MatDialog, MatDialogConfig} from "@angular/material/dialog";


@Component({
    selector: 'app-list',
    templateUrl: './shopping-lists.component.html',
    styleUrls: ['./shopping-lists.component.css']
})
export class ShoppingListsComponent implements OnInit, OnDestroy {
    userID: string;
    lists: ShoppingList[];
    account: Account;
    archived: boolean;
    menuSub: Subscription;

    constructor(private logger: NGXLogger,
                private listSrv: ListService,
                private activatedRoute: ActivatedRoute,
                private router: Router,
                private authSrv: AuthenticationService,
                private alertSrv: AlertService,
                private menuSrv: MenuActionsService,
                private dialog: MatDialog) {
    }

    ngOnInit() {
        this.menuSub = this.menuSrv.actionEmitted.subscribe(action => {
            switch (action) {
                case MenuAction.REFRESH:
                    this.loadUserLists();
                    break;
                case MenuAction.ADD:
                    this.newListOpenDialog();
                    break;
            }
        });
        this.account = this.authSrv.currentAccount;
        this.activatedRoute.params.subscribe(params => {
            this.userID = params['userid'];
        });
        this.activatedRoute.queryParams.subscribe(params => {
            this.archived = params['archived'];
            this.loadUserLists();
        })
    }

    ngOnDestroy(): void {
        this.menuSub.unsubscribe();
    }


    private loadUserLists() {
        this.listSrv.getUserList(this.userID, true, this.archived).subscribe(lists => {
            this.lists = lists;
        })
    }

    /**
     * Open new list dialog
     */
    newListOpenDialog() {
        this.listSrv.openNewListDialog().subscribe(newList => {
                if (newList) {
                    this.alertSrv.success("app.shopping.create.success");
                    this.router.navigate(['/list', newList.id]);
                }
            }
        );
    }

    /**
     * Edit list ( name and category sorting preset )
     * @param list list to be edited
     */
    editListOpenDialog(list: ShoppingList) {
        this.listSrv.openEditListDialog(list).subscribe(newList => {
                if (newList) {
                    this.alertSrv.success("app.shopping.update.success");
                }
            }
        );
    }

    /**
     * Open share list dialog
     * @param list list for which dialog should be opened
     */
    shareListOpenDialog(list: ShoppingList) {
        this.listSrv.openShareListDialog(list).subscribe(reply => {
                if (reply) {
                    this.loadUserLists();
                }
            }
        );
    }

    /**
     * Toggle archive status for list
     * @param list list for which status will be toggled
     * @param archived is list already archived ?
     */
    archiveToggle(list: ShoppingList, archived?: boolean) {
        this.listSrv.archive(list).subscribe(res => {
            if (res && res.archived != archived) {
                let msgKey = archived ? 'app.shopping.unarchive.success' : 'app.shopping.archive.success';
                this.alertSrv.success(msgKey);
                this.loadUserLists();
            } else {
                let msgKey = archived ? 'app.shopping.unarchive.fail' : 'app.shopping.archive.fail';
                this.alertSrv.error(msgKey);
            }
        })
    }

    /**
     * Leave shared list
     * @param list list which will be left
     */
    leaveShared(list: ShoppingList) {
        this.listSrv.archive(list).subscribe(res => {
            if (res) {
                this.alertSrv.success('app.shopping.share.leave.success');
                this.loadUserLists();
            }
        });
    }

    /**
     * Confirm deletion of list
     * Unlike any other events this actually requires confimration of deletion
     * @param list list which will be deleted after operation will be confirmed
     */
    confirmDeletion(list: ShoppingList) {
        const data: ConfirmDialog = {
            title_key: 'app.shopping.delete.confirm',
            message_key: 'app.shopping.delete.confirm.msg',
            action_key: 'app.general.delete',
            action_class: 'warn'
        };
        const dialogConfig: MatDialogConfig = {
            disableClose: true,
            panelClass: 'shopper-modal',
            data: data
        };
        let dialogRef = this.dialog.open(ConfirmDialogComponent, dialogConfig);

        dialogRef.afterClosed().subscribe(result => {
            if (result) {
                this.delete(list);
            }
        })
    }

    private delete(list: ShoppingList) {
        this.listSrv.delete(list).subscribe(() => {
            this.loadUserLists();
            this.alertSrv.success('app.shopping.delete.success');
        }, error => {
            this.alertSrv.success('app.shopping.delete.fal');
            this.logger.error(error);
        })
    }

    private copyList(list: ShoppingList) {
        this.listSrv.copyList(list).subscribe(result => {
            if (result) {
                this.alertSrv.success('app.shopping.copy.success', {name: list.name});
                this.router.navigate(['/list', result.id]);
            }
        }, error => {
            this.alertSrv.error('app,shopping.copy.error');
            this.logger.error(error)
        })
    }

}
