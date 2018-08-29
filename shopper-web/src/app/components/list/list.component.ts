import {Component, OnInit} from '@angular/core';
import {ListService} from "../../services/list.service";
import {ActivatedRoute} from "@angular/router";
import {ShoppingList} from "../../model/ShoppingList";
import {ListItem} from "../../model/ListItem";
import * as _ from 'lodash';

@Component({
    selector: 'app-list',
    templateUrl: './list.component.html',
    styleUrls: ['./list.component.css']
})
export class ListComponent implements OnInit {

    account: Account;
    listID: number;
    list: ShoppingList;
    items: ListItem[];
    done: ListItem[];

    constructor(private listSrv: ListService, private route: ActivatedRoute) {
    }

    ngOnInit() {
        this.route.params.subscribe(params => {
            this.listID = params['listid'];
            this.loadItems();
        });
    }

    private loadItems() {
        this.listSrv.getListByID(this.listID).subscribe(list => {
            this.list = list;
            this.done = _.filter(list.items, item => item.done);
            this.items = _.difference(list.items, this.done)
        });
    }
}
