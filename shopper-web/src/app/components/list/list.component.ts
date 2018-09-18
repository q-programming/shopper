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

    listID: number;
    list: ShoppingList;
    items: ListItem[];
    done: ListItem[];
    categories: CategoryOption[] = [];
    shareTooltip: string;
    sharedCount = 0;

    constructor(private listSrv: ListService,
                private itemSrv: ItemService,
                private route: ActivatedRoute,
                private alertSrv: AlertService,
                private translate: TranslateService) {
    }

    ngOnInit() {
        this.loadCategoriesWithLocalName();
        this.route.params.subscribe(params => {
            this.listID = params['listid'];
            this.loadItems();
        });
    }

    private getSharedButtonTootlip() {
        if (this.sharedCount > 0) {
            this.translate.get('app.shopping.share.with', {count: this.list.shared.length}).subscribe(text => this.shareTooltip = text);
        } else {
            this.translate.get('app.shopping.share').subscribe(text => this.shareTooltip = text)
        }
    }

    /**
     * Toggle passed item as done/not done
     * @param item item to be toggled
     */
    toggleItem(item: ListItem) {
        if (!this.list.archived) {
            this.itemSrv.toggleItem(this.listID, item).subscribe((result) => {
                if (result) {
                    _.find(this.list.items, i => i.id === result.id).done = result.done;
                    result.done ? this.list.done++ : this.list.done--;
                    this.sortDoneNotDone();
                }
            })
        }
    }

    /**
     * Open new item dialog
     */
    openNewItemDialog() {
        this.itemSrv.openNewItemDialog(this.listID).subscribe(list => {
            if (list) {
                this.assignListWithSorting(list);
                this.alertSrv.success("app.item.add.success");
            } else {
                this.alertSrv.error("app.item.add.error");
            }
        })
    }

    openEditItemDialog(item: ListItem) {
        this.itemSrv.openEditItemDialog(this.listID, Object.assign({}, item)).subscribe(list => {
            if (list) {
                this.assignListWithSorting(list);
                this.alertSrv.success("app.item.update.success");
            } else {
                this.alertSrv.error("app.item.update.fail");
            }
        })
    }

    /**
     * Update category of passed item
     * @param item item for which category will be updated
     * @param newCategory new category
     */
    updateCategory(item: ListItem, newCategory: Category) {
        item.category = newCategory;
        this.itemSrv.updateItem(this.listID, item).subscribe(list => {
            if (list) {
                this.alertSrv.success("app.item.category.updated");
                this.assignListWithSorting(list);
            }
        }, () => {
            this.alertSrv.success("app.item.category.fail");
        })
    }

    confirmDeletion(item: ListItem) {
        _.remove(this.items, (i) => {
            return i.id === item.id
        });
        this.alertSrv.undoable("app.item.delete.success").subscribe(undo => {
            if (undo !== undefined) {
                if (!undo) {
                    this.itemSrv.deleteItem(this.listID, item).subscribe((list) => {
                        if (list) {
                            this.assignListWithSorting(list);
                        }
                    });
                } else {
                    this.loadItems();
                }
            }
        });
    }

    shareListOpenDialog(list: ShoppingList) {
        this.listSrv.openShareListDialog(list).subscribe(reply => {
                if (reply) {
                    if (reply === 'SENDING') {
                        this.alertSrv.info("app.shopping.share.email.inqueue");
                    } else {
                        this.alertSrv.success("app.shopping.share.sent", {name: this.list.name, email: reply});
                        this.loadItems();
                    }
                }
            }
        );
    }


    private loadCategoriesWithLocalName() {
        Object.values(Category).map(value => {
            return this.translate.get(value.toString()).subscribe(name => {
                this.categories.push({
                    category: value,
                    name: name
                });
            })
        });
    }

    private loadItems() {
        this.listSrv.getListByID(this.listID).subscribe(list => {
            this.assignListWithSorting(list)
        });
    }

    quickAdd(list: ShoppingList) {
        this.assignListWithSorting(list);
        this.alertSrv.success("app.item.add.success");
    }

    private assignListWithSorting(list: ShoppingList) {
        this.list = list;
        this.sharedCount = list.shared.length;
        this.getSharedButtonTootlip();
        this.sortDoneNotDone();
    }

    private sortDoneNotDone() {
        this.done = _.filter(this.list.items, item => item.done);
        this.items = _.difference(this.list.items, this.done)
    }

}
