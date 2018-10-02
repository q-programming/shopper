import {Component, OnInit} from '@angular/core';
import {ListService} from "../../services/list.service";
import {ActivatedRoute, Router} from "@angular/router";
import {ShoppingList} from "../../model/ShoppingList";
import {AuthenticationService} from "../../services/authentication.service";
import {Account} from "../../model/Account";
import {AlertService} from "../../services/alert.service";
import {NGXLogger} from "ngx-logger";
import {MatDialog, MatDialogConfig} from "@angular/material";
import {ConfirmDialog, ConfirmDialogComponent} from "../dialogs/confirm/confirm-dialog.component";
import {ActionsService} from "../../services/actions.service";


@Component({
    selector: 'app-list',
    templateUrl: './shopping-lists.component.html',
    styleUrls: ['./shopping-lists.component.css']
})
export class ShoppingListsComponent implements OnInit {
    userID: string;
    lists: ShoppingList[];
    account: Account;
    archived: boolean;

    constructor(private logger: NGXLogger,
                private listSrv: ListService,
                private activatedRoute: ActivatedRoute,
                private router: Router,
                private authSrv: AuthenticationService,
                private alertSrv: AlertService,
                private refreshSrv: ActionsService,
                private dialog: MatDialog) {
        this.refreshSrv.refreshEmitted.subscribe(refresh => {
            if (refresh) {
                this.loadUserLists();
            }
        })
    }

    ngOnInit() {
        this.account = this.authSrv.currentAccount;
        this.activatedRoute.params.subscribe(params => {
            this.userID = params['userid'];
        });
        this.activatedRoute.queryParams.subscribe(params => {
            this.archived = params['archived'];
            this.loadUserLists();
        })
    }

    private loadUserLists() {
        this.listSrv.getUserList(this.userID, true, this.archived).subscribe(lists => {
            this.lists = lists;
        })
    }

    newListOpenDialog() {
        this.listSrv.openNewListDialog().subscribe(newList => {
                if (newList) {
                    this.alertSrv.success("app.shopping.create.success");
                    this.router.navigate(['/list', newList.id]);
                }
            }
        );
    }

    editListOpenDialog(list: ShoppingList) {
        this.listSrv.openEditListDialog(list).subscribe(newList => {
                if (newList) {
                    this.alertSrv.success("app.shopping.update.success");
                }
            }
        );
    }

    shareListOpenDialog(list: ShoppingList) {
        this.listSrv.openShareListDialog(list).subscribe(reply => {
                if (reply) {
                    this.loadUserLists();
                }
            }
        );
    }


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

    leaveShared(list: ShoppingList) {
        this.listSrv.archive(list).subscribe(res => {
            if (res) {
                this.alertSrv.success('app.shopping.share.leave.success');
                this.loadUserLists();
            }
        });
    }

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
}
