import {Component, OnInit} from '@angular/core';
import {CategoryOption} from "@model/CategoryOption";
import {ItemService} from "@services/item.service";
import {AuthenticationService} from "@services/authentication.service";

@Component({
    selector: 'en-help',
    templateUrl: './en-help.component.html',
    styleUrls: ['../help.component.css']
})
export class EnHelpComponent implements OnInit {
    categories: CategoryOption[] = [];
    isAdmin: boolean;


    constructor(private itemSrv: ItemService,
                private authSrv: AuthenticationService) {
    }

    ngOnInit() {
        this.categories = this.itemSrv.categories;
        this.isAdmin = this.authSrv.isAdmin();
    }
}
