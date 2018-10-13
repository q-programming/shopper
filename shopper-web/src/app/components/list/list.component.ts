import {Component, OnDestroy, OnInit} from '@angular/core';
import {ListService} from "../../services/list.service";
import {ActivatedRoute, Router} from "@angular/router";
import {ShoppingList} from "../../model/ShoppingList";
import {ListItem} from "../../model/ListItem";
import * as _ from 'lodash';
import {AlertService} from "../../services/alert.service";
import {Category} from "../../model/Category";
import {CategoryOption} from "../../model/CategoryOption";
import {TranslateService} from "@ngx-translate/core";
import {Observable, Subscription} from "rxjs";
import {MenuAction, MenuActionsService} from "../../services/menu-actions.service";
import {ItemService} from "../../services/item.service";


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
    inProgress: boolean;


    constructor(private listSrv: ListService,
                private itemSrv: ItemService,
                private router: Router,
                private activatedRoute: ActivatedRoute,
                private alertSrv: AlertService,
                private menuSrv: MenuActionsService,
                private translate: TranslateService) {
        //handle menu srv actions
        this.menuSrv.actionEmitted.subscribe(action => {
            switch (action) {
                case MenuAction.REFRESH:
                    //Action usually comes from current user so disable any potential notifications
                    this.inProgress = true;
                    this.loadItems();
                    break;
                case MenuAction.SHARE:
                    this.shareListOpenDialog();
                    break;
                case MenuAction.ADD_ITEM:
                    this.openNewItemDialog();
                    break;
                case MenuAction.EDIT:
                    this.openEditListDialog();
                    break;
                case MenuAction.CLEANUP:
                    this.cleanup();
                    break;
                case MenuAction.ARCHIVE:
                    this.archiveToggle(this.list.archived);
                    break;
                case MenuAction.LEAVE:
                    this.leaveShared();
                    break;
            }
        });
    }

    ngOnInit() {
        this.categories = this.itemSrv.categories;
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
        this.stopListWatcher();
        this.itemSrv.openNewItemDialog(this.listID).subscribe(list => {
            if (list) {
                this.assignListWithSorting(list);
                this.alertSrv.success("app.item.add.success");
            } else {
                this.inProgress = true;
                this.loadItems();
            }
            this.startListWatcher();
        })
    }

    /**
     * Open edit dialog
     * @param item item to be edited
     */
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
        if (newCategory !== item.category) {
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
    }

    /**
     * Delete item. First there will be undoable message shown . After timer is done , perform actual deletion
     * @param item item to be deleted after undoable timer
     */
    deleteItem(item: ListItem) {
        this.stopListWatcher();
        _.remove(this.items, (i) => {
            return i.id === item.id
        });
        this.inProgress = true;
        this.alertSrv.undoable("app.item.delete.success", {name: item.product.name}).subscribe(undo => {
            if (undo !== undefined && !undo) {
                this.itemSrv.deleteItem(this.listID, item).subscribe((list) => {
                    if (list) {
                        if (!this.inProgress) {
                            this.assignListWithSorting(list);
                        }
                        this.inProgress = true;
                        this.startListWatcher();
                    }
                });
            } else if (undo !== undefined && undo) {
                this.loadItems();
            }
        });
    }

    /**
     * Open share list dialog
     */
    shareListOpenDialog() {
        this.listSrv.openShareListDialog(this.list).subscribe(reply => {
                if (reply) {
                    this.loadItems();
                }
            }
        );
    }

    /**
     * Open edit list dialog
     */
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

    private loadItems() {
        this.listSrv.getListByID(this.listID).subscribe(list => {
            if (this.list && !this.inProgress) {
                this.wereItemsChangedByShared(list);
            }
            this.inProgress = false;//clear any potential cleanup flag
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

    /**
     * Handler for child element which quickly added some item
     * Main purpose is just to refresh items based on loaded list
     * @param list list to be refreshed
     */
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

    /**
     * Start edit process if list is not archived
     */
    startEdit() {
        if (!this.list.archived) {
            this.edit = true;
        }
    }

    /**
     * Change list name if new value is actually different than current one
     */
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

    /**
     * Toggle archive status for current list
     * @param archived is current list archived already ?
     */
    archiveToggle(archived?: boolean) {
        this.stopListWatcher();
        this.listSrv.archive(this.list).subscribe(res => {
            if (res) {
                let msgKey = archived ? 'app.shopping.unarchive.success' : 'app.shopping.archive.success';
                this.alertSrv.success(msgKey);
            }
        }, error => {
            let msgKey = archived ? 'app.shopping.unarchive.fail' : 'app.shopping.archive.fail';
            this.alertSrv.error(msgKey);
        }, () => {
            this.loadItems();
        })
    }

    /**
     * Leave shared list
     */
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

    /**
     * Cleanup all done items. Actual cleanup API call is performed after undoable timeout (or alert dismiss )
     */
    cleanup() {
        this.stopListWatcher();
        this.inProgress = true;
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
        let percentage = this.list.items.length > 0 ? (this.done.length / this.list.items.length) * 100 : 0;
        return parseFloat(`${percentage}`).toFixed(2);
    }

}
