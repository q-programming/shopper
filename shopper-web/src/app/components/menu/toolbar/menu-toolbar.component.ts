import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {AuthenticationService} from "@services/authentication.service";
import {ListService} from "@services/list.service";
import {SideNavAction} from "../sidenav/menu-side-nav.component";
import {MenuAction, MenuActionsService} from "@services/menu-actions.service";

@Component({
    selector: 'menu-toolbar',
    templateUrl: './menu-toolbar.component.html',
    styleUrls: ["./menu-toolbar.component.css"]
})
export class MenuToolbarComponent implements OnInit {

    @Output() sidenav = new EventEmitter<SideNavAction>();
    @Input() onListView: boolean;
    @Input() account: Account;
    @Input() loggedIn: boolean;

    constructor(private authSrv: AuthenticationService,
                private listSrv: ListService,
                private menuSrv: MenuActionsService) {
    }

    ngOnInit() {
    }

    openNewItemDialog() {
        this.menuSrv.emmitAction(MenuAction.ADD_ITEM);
    }

    toggleSideNav() {
        this.sidenav.emit(SideNavAction.TOGGLE);
    }


}
