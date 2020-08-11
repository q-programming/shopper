import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {NGXLogger} from "ngx-logger";
import {ListService} from "@services/list.service";
import {ItemService} from "@services/item.service";
import {ActivatedRoute, Router} from "@angular/router";
import {AlertService} from "@services/alert.service";
import {MenuActionsService} from "@services/menu-actions.service";
import {AuthenticationService} from "@services/authentication.service";
import {TranslateService} from "@ngx-translate/core";
import {DeviceDetectorService} from "ngx-device-detector";
import {Account} from "@model/Account";
import {ListItem} from "@model/ListItem";
import {Category} from "@model/Category";
import {itemDisplayName} from "../../../utils/utils";
import {CategoryOption} from "@model/CategoryOption";

@Component({
    selector: 'Item',
    templateUrl: './item.component.html',
    styleUrls:['./item.component.css']
})
export class ItemComponent implements OnInit {

    @Input()
    archived: boolean;
    @Input()
    item: ListItem;
    @Input()
    categories: CategoryOption[];

    @Output()
    toggle = new EventEmitter<ListItem>();
    @Output()
    edit = new EventEmitter<ListItem>();
    @Output()
    delete = new EventEmitter<ListItem>();
    @Output()
    updateCat = new EventEmitter<object>();

    isMobile: boolean;
    currentAccount: Account;

    constructor(private logger: NGXLogger,
                private listSrv: ListService,
                private itemSrv: ItemService,
                private router: Router,
                private activatedRoute: ActivatedRoute,
                private alertSrv: AlertService,
                private menuSrv: MenuActionsService,
                private authSrv: AuthenticationService,
                private translate: TranslateService,
                private deviceService: DeviceDetectorService) {
        this.currentAccount = this.authSrv.currentAccount;
        this.isMobile = deviceService.isMobile();
    }

    ngOnInit() {
    }

    toggleItem(item: ListItem, event: MouseEvent) {
        event.preventDefault();
        this.toggle.emit(item);
    }

    displayName(item: ListItem): string {
        return itemDisplayName(item)
    }

    openEditItemDialog(item: ListItem) {
        this.edit.emit(item);
    }

    deleteItem(item: ListItem) {
        this.delete.emit(item);
    }

    updateCategory(item: ListItem, newCategory: Category) {
        this.updateCat.emit({item: item, category: newCategory})
    }
}
