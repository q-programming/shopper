import {Component, OnInit} from '@angular/core';
import {ListService} from "../../services/list.service";
import {ActivatedRoute} from "@angular/router";
import {ShoppingList} from "../../model/ShoppingList";
import {ListItem} from "../../model/ListItem";
import * as _ from 'lodash';
import {ItemService} from "../../services/item.service";
import {AlertService} from "../../services/alert.service";
import {Category} from "../../model/Category";
import {CategoryOption} from "../../model/CategoryOption";
import {TranslateService} from "@ngx-translate/core";

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
    categories: CategoryOption[];

    constructor(private listSrv: ListService,
                private itemSrv: ItemService,
                private route: ActivatedRoute,
                private alertSrv: AlertService,
                private translate: TranslateService) {

        this.categories = Object.values(Category).map(value => {
            return {
                category: value,
                name: this.translate.instant(value.toString())
            }
        });
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
                result.done ? this.list.done++ : this.list.done--;
                this.sortDoneNotDone();
            }
        })
    }

    openNewItemDialog() {
        this.itemSrv.openNewItemDialog(this.listID).subscribe(list => {
            if (list) {
                this.list = list;
                this.sortDoneNotDone();
                this.alertSrv.success("app.item.add.success");
            } else {
                this.alertSrv.error("app.item.add.error");
            }
        })
    }

    updateCategory(item: ListItem, newCategory: Category) {
        item.category = newCategory;
        this.itemSrv.updateItem(this.listID, item).subscribe(item => {
            if (item) {
                this.alertSrv.success("app.item.category.updated");
                this.loadItems()
            }
        })
    }
}
