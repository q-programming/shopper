import {Component, OnInit} from '@angular/core';
import {ListService} from "../../services/list.service";
import {ActivatedRoute} from "@angular/router";
import {ShoppingList} from "../../model/ShoppingList";
import {AuthenticationService} from "../../services/authentication.service";
import {Account} from "../../model/Account";


@Component({
    selector: 'app-list',
    templateUrl: './shopping-lists.component.html',
    styleUrls: ['./shopping-lists.component.css']
})
export class ShoppingListsComponent implements OnInit {
    userID: string;
    lists: ShoppingList[];
    account: Account;

    constructor(private listSrv: ListService, private route: ActivatedRoute, private authSrv: AuthenticationService) {
    }

    ngOnInit() {
        this.account = this.authSrv.currentAccount;
        this.route.params.subscribe(params => {
            this.userID = params['userid'];
            this.listSrv.getUserList(this.userID, true).subscribe(lists => {
                this.lists = lists;
            })
        });
    }
}
