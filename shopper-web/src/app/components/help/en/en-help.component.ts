import {Component, OnInit} from '@angular/core';
import {ScrollToConfigOptions, ScrollToService} from "@nicky-lenaers/ngx-scroll-to";
import {ActivatedRoute} from "@angular/router";
import {CategoryOption} from "@model/CategoryOption";
import {ItemService} from "@services/item.service";
import {AuthenticationService} from "@services/authentication.service";

@Component({
    selector: 'en-help',
    templateUrl: './en-help.component.html',
    styleUrls: ['../help.component.css']
})
export class EnHelpComponent implements OnInit {
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
