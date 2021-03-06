import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {AuthenticationService} from "@services/authentication.service";
import {Account} from "@model/Account";
import {ListService} from "@services/list.service";
import {ShoppingList} from "@model/ShoppingList";
import {AlertService} from "@services/alert.service";
import {SideNavAction} from "./components/menu/sidenav/menu-side-nav.component";

@Component({
    selector: 'app-root',
    templateUrl: 'app.component.html',
    styles: []
})
export class AppComponent implements OnInit {
    shown: boolean;
    account: Account;
    message_count = {count: ""};
    lists: ShoppingList[];
    list: ShoppingList;
    isInProgress: boolean = false;

    constructor(private router: Router,
                private activatedRoute: ActivatedRoute,
                private authSrv: AuthenticationService,
                private listSrv: ListService,
                private alertSrv: AlertService) {


        router.events.subscribe(() => {
            this.account = this.authSrv.currentAccount;
        });
        this.listSrv.listEmiter.subscribe(list => {
            this.list = list;
        })
    }

    ngOnInit() {
        this.account = this.authSrv.currentAccount;
        if (this.account) {
            this.message_count.count = "" + 1;
            this.getUserLists();
        }
    }

    private getUserLists() {
        this.listSrv.getMyLists().subscribe(lists => this.lists = lists.splice(0, 4));
    }

    openNewListDialog() {
        this.listSrv.openNewListDialog().subscribe(res => {
                if (res) {
                    this.getUserLists();
                    this.router.navigate(['/list', res.id]);
                    this.alertSrv.success("app.shopping.create.success");
                } else {
                    this.alertSrv.error("app.shopping.create.fail");
                }
            }
        );
    }

    get onListView(): boolean {
        return this.router.url.includes('/list')
    }

    get loggedIn() {
        return !!this.authSrv.currentAccount;
    }


    handleSideNav($event: SideNavAction) {
        switch ($event) {
            case SideNavAction.CLOSE:
                this.shown = false;
                break;
            case SideNavAction.TOGGLE:
                if (this.shown) {
                    this.getUserLists();
                }
                this.shown = !this.shown;
                break;
            case SideNavAction.OPEN:
                this.shown = true;
                break;
        }
    }

    onPull() {
        console.log("Refresh me!");
        this.isInProgress = true;
    }
}

