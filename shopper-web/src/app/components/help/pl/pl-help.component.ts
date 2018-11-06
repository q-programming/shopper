import {Component, OnInit} from '@angular/core';
import {CategoryOption} from "@model/CategoryOption";
import {ActivatedRoute} from "@angular/router";
import {ScrollToConfigOptions, ScrollToService} from "@nicky-lenaers/ngx-scroll-to";
import {ItemService} from "@services/item.service";
import {AuthenticationService} from "@services/authentication.service";

@Component({
    selector: 'pl-help',
    templateUrl: './pl-help.component.html',
    styleUrls: ['../help.component.css']
})
export class PlHelpComponent implements OnInit {
    fragment: string;
    categories: CategoryOption[] = [];
    isAdmin: boolean;

    constructor(private route: ActivatedRoute,
                private scrollToService: ScrollToService,
                private itemSrv: ItemService,
                private authSrv: AuthenticationService) {
    }

    ngOnInit() {
        this.categories = this.itemSrv.categories;
        this.route.fragment.subscribe(fragment => {
            this.fragment = fragment;
            const config: ScrollToConfigOptions = {
                target: this.fragment,
                offset: -70
            };
            this.scrollToService.scrollTo(config);
            this.fragment = undefined
        });
        this.isAdmin = this.authSrv.isAdmin();
    }

}
