import {Component, OnDestroy, OnInit} from '@angular/core';
import {ListService} from "../../services/list.service";
import {ActivatedRoute, Router} from "@angular/router";
import {ShoppingList} from "../../model/ShoppingList";
import {ListItem} from "../../model/ListItem";
import * as _ from 'lodash';
import {ItemService} from "../../services/item.service";
import {AlertService} from "../../services/alert.service";
import {Category} from "../../model/Category";
import {CategoryOption} from "../../model/CategoryOption";
import {TranslateService} from "@ngx-translate/core";
import {Observable, Subscription} from "rxjs";

@Component({
    selector: 'app-list',
    templateUrl: './list.component.html',
    styleUrls: ['./list.component.css']
})
export class ListComponent implements OnInit, OnDestroy {

    listID: number;
    list: ShoppingList;
    items: ListItem[];
    done: ListItem[];
    categories: CategoryOption[] = [];
    shareTooltip: string;
    listName: string;
    edit: boolean;
    sharedCount = 0;
    sub: Subscription;
    cleanupInProgress: boolean;

    constructor(private listSrv: ListService,
                private itemSrv: ItemService,
                private router: Router,
                private activatedRoute: ActivatedRoute,
                private alertSrv: AlertService,
                private translate: TranslateService) {
    }

    ngOnInit() {
        this.loadCategoriesWithLocalName();
        this.activatedRoute.params.subscribe(params => {
            this.listID = params['listid'];
            this.loadItems();
        });
        this.activatedRoute.queryParams.subscribe(params => {
            this.edit = params['edit'];
        });
    }

    ngOnDestroy(): void {
        this.stopListWatcher();
    }


    private startListWatcher() {
        if (this.list.shared.length > 0 && !this.list.archived) {
            this.stopListWatcher();
            this.sub = Observable.interval(10000)
                .subscribe((val) => {
                    this.loadItems();
                });
        }
    }

    private stopListWatcher() {
        if (this.sub) {
            this.sub.unsubscribe();
        }
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
                    this.sortDoneNotDone();
                }
            })
        }
    }

    /**
     * Open new item dialog
     */
    openNewItemDialog() {
        // this.stopListWatcher();
        this.itemSrv.openNewItemDialog(this.listID).subscribe(list => {
            if (list) {
                this.assignListWithSorting(list);
                this.alertSrv.success("app.item.add.success");
            } else {
                this.alertSrv.error("app.item.add.error");
            }
            // this.startListWatcher();
        })
    }

    openEditItemDialog(item: ListItem) {
        // this.stopListWatcher();
        this.itemSrv.openEditItemDialog(this.listID, Object.assign({}, item)).subscribe(list => {
            if (list) {
                this.assignListWithSorting(list);
                this.alertSrv.success("app.item.update.success");
            } else {
                this.alertSrv.error("app.item.update.fail");
            }
            // this.startListWatcher();
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

    deleteItem(item: ListItem) {
        this.stopListWatcher();
        _.remove(this.items, (i) => {
            return i.id === item.id
        });
        this.alertSrv.undoable("app.item.delete.success").subscribe(undo => {
            if (undo !== undefined) {
                if (!undo) {
                    this.itemSrv.deleteItem(this.listID, item).subscribe((list) => {
                        if (list) {
                            this.assignListWithSorting(list);
                            this.startListWatcher();
                        }
                    });
                } else {
                    this.loadItems();
                }
            }
        });
    }


    shareListOpenDialog() {
        this.listSrv.openShareListDialog(this.list).subscribe(reply => {
                if (reply) {
                    this.loadItems();
                }
            }
        );
    }

    openEditListDialog() {
        // this.stopListWatcher();
        this.listSrv.openEditListDialog(this.list).subscribe(reply => {
            if (reply) {
                this.alertSrv.success('app.shopping.update.success');
                this.loadItems();
            }
        }, error => {
            this.alertSrv.error('app.shopping.update.fail');
        })
    }


    private loadCategoriesWithLocalName() {
        Object.values(Category).map(value => {
            return this.translate.get(value.toString()).subscribe(name => {
                this.categories.push({
                    category: value,
                    name: name
                });
            }, undefined, () => {
                this.categories.sort((a, b) => a.name.localeCompare(b.name))
            })
        });
    }

    private loadItems() {
        this.listSrv.getListByID(this.listID).subscribe(list => {
            if (this.list && !this.cleanupInProgress) {
                this.wereItemsChangedByShared(list);
            }
            this.cleanupInProgress = false;//clear any potential cleanup flag
            this.listName = list.name;
            this.assignListWithSorting(list);
            this.startListWatcher();
        });
    }

    private wereItemsChangedByShared(list) {
        let itemsDiff = this.list.items.length - list.items.length;
        if (itemsDiff == -1) {
            this.alertSrv.info('app.item.new.one');
        } else if (itemsDiff < -1) {
            this.alertSrv.info('app.item.new.many', {count: Math.abs(itemsDiff)});
        } else if (itemsDiff > 0) {
            this.alertSrv.info('app.item.removed.many', {count: Math.abs(itemsDiff)});
        }
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

    startEdit() {
        if (!this.list.archived) {
            this.edit = true;
        }
    }

    editListName() {
        if (this.listName !== this.list.name) {
            this.list.name = this.listName;
            this.listSrv.updateList(this.list).subscribe(list => {
                this.alertSrv.success('app.shopping.name.success');
                this.list.name = list.name;
            }, error => {
                this.alertSrv.error('app.shopping.name.fail')
            })
        }
        this.edit = false;
    }

    archiveToggle(archived?: boolean) {
        this.stopListWatcher();
        this.listSrv.archive(this.list).subscribe(res => {
            if (res) {
                let msgKey = archived ? 'app.shopping.unarchive.success' : 'app.shopping.archive.success';
                this.alertSrv.success(msgKey);
                this.loadItems();
            }
        }, error => {
            let msgKey = archived ? 'app.shopping.unarchive.fail' : 'app.shopping.archive.fail';
            this.alertSrv.error(msgKey);
            this.startListWatcher();
        })
    }

    leaveShared() {
        this.alertSrv.undoable('app.shopping.share.leave.success').subscribe(undo => {
            if (undo !== undefined) {
                if (!undo) {
                    this.listSrv.archive(this.list).subscribe(res => {
                        if (res) {
                            this.router.navigate(['/']);
                        }
                    });
                }
            }
        });
    }


    cleanup() {
        this.stopListWatcher();
        this.cleanupInProgress = true;
        this.done = [];
        this.list.done = 0;
        this.list.items = this.items;
        this.alertSrv.undoable("app.shopping.cleanup").subscribe(undo => {
            if (undo !== undefined) {
                if (!undo) {
                    this.listSrv.cleanup(this.listID).subscribe((list) => {
                        if (list) {
                            this.assignListWithSorting(list);
                            this.startListWatcher();
                        }
                    });
                } else {
                    this.loadItems();
                }
            }
        });
    }

    get percentage() {
        let perc = this.list.items.length > 0 ? (this.done.length / this.list.items.length) * 100 : 0;
        return parseFloat(`${perc}`).toFixed(2);
    }

}
