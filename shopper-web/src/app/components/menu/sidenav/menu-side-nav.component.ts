import {AfterViewInit, Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {ShoppingList} from "@model/ShoppingList";
import {Account} from "@model/Account";
import {AuthenticationService} from "@services/authentication.service";
import {Router} from "@angular/router";
import {MenuAction, MenuActionsService} from "@services/menu-actions.service";

@Component({
    selector: 'menu-sidenav',
    templateUrl: './menu-side-nav.component.html',
    styleUrls: ['./menu-side-nav.component.css']
})
export class MenuSideNavComponent implements OnInit {

    @Output() sidenav = new EventEmitter<SideNavAction>();
    @Output() newList = new EventEmitter<boolean>();
    @Input() list: ShoppingList;
    @Input() lists: ShoppingList[];
    @Input() shown: boolean;
    account: Account;

    constructor(private router: Router,
                private menuSrv: MenuActionsService,
                private authSrv: AuthenticationService) {
        this.router.events.subscribe(() => this.sidenav.emit(SideNavAction.CLOSE))
    }

    ngOnInit() {
        this.account = this.authSrv.currentAccount;
    }


    openNewListDialog() {
        this.sidenav.emit(SideNavAction.CLOSE);
        this.menuSrv.emmitAction(MenuAction.ADD);
    }

    openNewItemDialog() {
        this.sidenav.emit(SideNavAction.CLOSE);
        this.menuSrv.emmitAction(MenuAction.ADD_ITEM);
    }

    openEditListDialog() {
        this.sidenav.emit(SideNavAction.CLOSE);
        this.menuSrv.emmitAction(MenuAction.EDIT);
    }

    shareListOpenDialog() {
        this.sidenav.emit(SideNavAction.CLOSE);
        this.menuSrv.emmitAction(MenuAction.SHARE);
    }

    cleanup() {
        this.sidenav.emit(SideNavAction.CLOSE);
        this.menuSrv.emmitAction(MenuAction.CLEANUP);
    }

    archiveToggle() {
        this.sidenav.emit(SideNavAction.CLOSE);
        this.menuSrv.emmitAction(MenuAction.ARCHIVE);
    }

    leaveShared() {
        this.sidenav.emit(SideNavAction.CLOSE);
        this.menuSrv.emmitAction(MenuAction.LEAVE);
    }

    refreshList() {
        this.sidenav.emit(SideNavAction.CLOSE);
        this.menuSrv.emmitAction(MenuAction.REFRESH);
    }

    copyList() {
        this.sidenav.emit(SideNavAction.CLOSE);
        this.menuSrv.emmitAction(MenuAction.COPY);
    }

    logout() {
        this.authSrv.logout().subscribe(() => {
            this.router.navigate(['/login']);
        });
    }

    closed() {
        this.sidenav.emit(SideNavAction.CLOSE);
    }

    get onListView(): boolean {
        return this.router.url.includes('/list')
    }

}

export enum SideNavAction {
    CLOSE, TOGGLE, OPEN
}
