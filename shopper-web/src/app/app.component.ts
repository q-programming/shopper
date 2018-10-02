import {Component, OnInit, ViewChild} from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {HttpClient} from "@angular/common/http";
import {AuthenticationService} from "./services/authentication.service";
import {Account} from "./model/Account";
import {MatSidenav} from "@angular/material";
import {ListService} from "./services/list.service";
import {ShoppingList} from "./model/ShoppingList";
import {AlertService} from "./services/alert.service";
import {Action, ActionsService} from "./services/actions.service";
import {ItemService} from "./services/item.service";

@Component({
    selector: 'app-root',
    templateUrl: 'app.component.html',
    styles: []
})
export class AppComponent implements OnInit {

    account: Account;
    message_count = {count: ""};
    lists: ShoppingList[];
    list: ShoppingList;
    @ViewChild("sidenav")
    private sidenav: MatSidenav;

    constructor(private http: HttpClient,
                private router: Router,
                private activatedRoute: ActivatedRoute,
                private authSrv: AuthenticationService,
                private listSrv: ListService,
                private itemSrv: ItemService,
                private alertSrv: AlertService,
                private actionsService: ActionsService) {
        router.events.subscribe(() => {
            if (this.sidenav && this.sidenav.opened) {
                this.sidenav.close();
            }
        });
        this.listSrv.listEmiter.subscribe(list => {
            this.list = list;
        })
    }

    ngOnInit() {
        this.account = this.authSrv.currentAccount;
        if (this.account) {
            this.message_count.count = "" + 1;
            this.listSrv.getUserList().subscribe(lists => this.lists = lists.splice(0, 4));
        }
    }

    loggedIn() {
        return !!this.authSrv.currentAccount;
    }

    logout() {
        this.authSrv.logout().subscribe(() => {
            this.router.navigate(['/login']);
        });
    }

    openNewListDialog() {
        this.sidenav.close();
        this.listSrv.openNewListDialog().subscribe(res => {
                if (res) {
                    this.router.navigate(['/list', res.id]);
                    this.alertSrv.success("app.shopping.create.success");
                } else {
                    this.alertSrv.error("app.shopping.create.fail");
                }
            }
        );
    }

    openNewItemDialog() {
        this.sidenav.close();
        this.actionsService.emmitAction(Action.ADD);
    }

    openEditListDialog() {
        this.sidenav.close();
        this.actionsService.emmitAction(Action.EDIT);
    }

    shareListOpenDialog() {
        this.sidenav.close();
        this.actionsService.emmitAction(Action.SHARE);
    }

    cleanup() {
        this.sidenav.close();
        this.actionsService.emmitAction(Action.CLEANUP);
    }

    archiveToggle() {
        this.sidenav.close();
        this.actionsService.emmitAction(Action.ARCHIVE);
    }

    leaveShared() {
        this.sidenav.close();
        this.actionsService.emmitAction(Action.LEAVE);
    }

    get onListView(): boolean {
        return this.router.url.includes('/list')
    }
}

