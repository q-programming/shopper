import {Component, OnInit} from '@angular/core';
import {ListService} from "../../services/list.service";
import {ActivatedRoute} from "@angular/router";
import {ShoppingList} from "../../model/ShoppingList";
import {ListItem} from "../../model/ListItem";
import * as _ from 'lodash';
import {ItemService} from "../../services/item.service";

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

    constructor(private listSrv: ListService,
                private itemSrv: ItemService,
                private route: ActivatedRoute) {
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
            this.sortDoneNotDone();
        });
    }

    private sortDoneNotDone() {
        this.done = _.filter(this.list.items, item => item.done);
        this.items = _.difference(this.list.items, this.done)
    }

    toggleItem(item: ListItem) {
        this.itemSrv.toggleItem(this.listID, item).subscribe((result) => {
            if (result) {
                _.find(this.list.items, i => i.id === result.id).done = result.done;
                this.sortDoneNotDone();
            }
        })
    }
}
