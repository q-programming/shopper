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
import {ConfirmDialog, ConfirmDialogComponent} from "../dialogs/confirm/confirm-dialog.component";
import {MatDialog, MatDialogConfig} from "@angular/material";

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

    constructor(private listSrv: ListService,
                private itemSrv: ItemService,
                private route: ActivatedRoute,
                private alertSrv: AlertService,
                private translate: TranslateService,
                private dialog: MatDialog) {
    }

    ngOnInit() {
        this.loadCategoriesWithLocalName();
        this.route.params.subscribe(params => {
            this.listID = params['listid'];
            this.loadItems();
        });
    }

    /**
     * Toggle passed item as done/not done
     * @param item item to be toggled
     */
    toggleItem(item: ListItem) {
        this.itemSrv.toggleItem(this.listID, item).subscribe((result) => {
            if (result) {
                _.find(this.list.items, i => i.id === result.id).done = result.done;
                result.done ? this.list.done++ : this.list.done--;
                this.sortDoneNotDone();
            }
        })
    }

    /**
     * Open new item dialog
     */
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

    openEditItemDialog(item: ListItem) {
        this.itemSrv.openEditItemDialog(this.listID, Object.assign({}, item)).subscribe(list => {
            if (list) {
                this.list = list;
                this.sortDoneNotDone();
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
                this.list = list;
                this.sortDoneNotDone();
            }
        }, () => {
            this.alertSrv.success("app.item.category.fail");
        })
    }

    confirmDeletion(item: ListItem) {
        const data: ConfirmDialog = {
            title_key: 'app.item.delete.confirm',
            message_key: 'app.item.delete.confirm.msg',
            action_key: 'app.general.delete',
            action_class: 'warn'
        };
        const dialogConfig: MatDialogConfig = {
            disableClose: true,
            panelClass: 'shopper-modal',
            data: data
        };
        let dialogRef = this.dialog.open(ConfirmDialogComponent, dialogConfig);
        dialogRef.afterClosed().subscribe(result => {
            if (result) {
                this.itemSrv.deleteItem(this.listID, item).subscribe((list) => {
                    this.alertSrv.success("app.item.delete.success");
                    this.list = list;
                    this.sortDoneNotDone();
                });
            }
        })
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
            this.list = list;
            this.sortDoneNotDone();
        });
    }

    private sortDoneNotDone() {
        this.done = _.filter(this.list.items, item => item.done);
        this.items = _.difference(this.list.items, this.done)
    }


}
