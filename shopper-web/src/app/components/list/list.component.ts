import {Component, HostListener, OnDestroy, OnInit} from '@angular/core';
import {ListService} from "@services/list.service";
import {AlertService} from "@services/alert.service";
import {MenuAction, MenuActionsService} from "@services/menu-actions.service";
import {ItemService} from "@services/item.service";
import {AuthenticationService} from "@services/authentication.service";
import {ActivatedRoute, Router} from "@angular/router";
import {ShoppingList} from "@model/ShoppingList";
import {ListItem} from "@model/ListItem";
import {Account} from "@model/Account";
import {CategoryOption} from "@model/CategoryOption";
import {WSAction, WSActionType} from "@model/WSAction";
import {environment} from "@env/environment";
import * as Stomp from 'stompjs';
import * as SockJS from 'sockjs-client';
import * as _ from 'lodash';
import {TranslateService} from "@ngx-translate/core";
import {NGXLogger} from "ngx-logger";
import {Subscription} from "rxjs";
import {DeviceDetectorService} from "ngx-device-detector";


@Component({
    selector: 'app-list',
    templateUrl: './list.component.html',
    styleUrls: ['./list.component.css']
})
export class ListComponent implements OnInit, OnDestroy {
    isShow: boolean;
    topPosToStartShowing = 150;

    listID: number;
    list: ShoppingList;
    items: ListItem[];
    firstDoneIndex: number;
    categories: CategoryOption[] = [];
    shareTooltip: string;
    listName: string;
    edit: boolean;
    sharedCount = 0;
    refreshPending: boolean;
    isInProgress: boolean = false;
    currentAccount: Account;
    stompClient;
    menuSub: Subscription;
    isMobile: boolean;
    public innerHeight: any;
    limiter;
    ACTIONS = {
        [MenuAction.PENDING_REFRESH]: () => this.delayRefresh(),
        [MenuAction.REFRESH]: () => {
            this.isInProgress = true;
            this.loadItems()
        },
        [MenuAction.SHARE]: () => this.shareListOpenDialog(),
        [MenuAction.ADD_ITEM]: () => this.openNewItemDialog(),
        [MenuAction.EDIT]: () => this.openEditListDialog(),
        [MenuAction.CLEANUP]: () => this.cleanup(),
        [MenuAction.LEAVE]: () => this.leaveShared(),
        [MenuAction.COPY]: () => this.copyList(),
    };
    SM_LIMIT = 20;
    MD_LIMIT = 30;
    XL_LIMIT = 40;

    constructor(private logger: NGXLogger,
                private listSrv: ListService,
                private itemSrv: ItemService,
                private router: Router,
                private activatedRoute: ActivatedRoute,
                private alertSrv: AlertService,
                private menuSrv: MenuActionsService,
                private authSrv: AuthenticationService,
                private translate: TranslateService,
                private deviceService: DeviceDetectorService) {
        this.currentAccount = this.authSrv.currentAccount;
        this.isMobile = deviceService.isMobile();
    }

    @HostListener('window:scroll')
    checkScroll() {
        const scrollPosition = window.pageYOffset || document.documentElement.scrollTop || document.body.scrollTop || 0;
        this.isShow = scrollPosition >= this.topPosToStartShowing;
    }

    ngOnInit() {
        this.innerHeight = window.innerHeight;
        this.limiter = this.initialLimiter();
        console.log(this.limiter);
        this.menuSub = this.menuSrv.actionEmitted.subscribe(action => {
            if (this.ACTIONS.hasOwnProperty(action)) {
                this.ACTIONS[action]()
            }
        });
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
        if (this.stompClient) {
            this.stompClient.disconnect();
        }
        this.menuSub.unsubscribe();
    }

    private initialLimiter() {
        if (this.innerHeight < 1000) {
            return this.SM_LIMIT;
        } else if (1000 < this.innerHeight && this.innerHeight < 1300) {
            return this.MD_LIMIT
        } else {
            return this.XL_LIMIT
        }
    }

    @HostListener('window:resize', ['$event'])
    onResize() {
        this.innerHeight = window.innerHeight;
        const newLimiter = this.initialLimiter();
        if (newLimiter != this.limiter) {
            this.limiter = newLimiter;
            this.loadItems();
        }
    }

    private loadItems() {
        this.listSrv.getListByID(this.listID).subscribe(list => {
            this.listName = list.name;
            this.assignListWithSorting(list as ShoppingList);
            //if list is shared with at least one account, init websocket
            if (this.sharedCount > 0 && !this.stompClient) {
                this.initializeWebSocketConnection();
            }
            this.isInProgress = false;
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
                    item.done = result.done;
                    this.shiftToggledItem(item);
                    this.sendWSRefresh();
                    this.listSrv.emitList(this.list);
                }
            })
        }
    }

    /**
     * When Item was marked as done/undone remve it from current items position, and move it to firstDoneIndex position
     * If view has not loaded it yet ( for example on smaller device ) , just move it to global list.items instead
     * @param item Item to be shifted
     */
    private shiftToggledItem(item: ListItem) {
        const oldIndex = _.findIndex(this.items, it => it.id === item.id);
        this.items.splice(oldIndex, 1);
        //If view already passed not done/done border, index have to be searched again due to adding and removing
        // from spliced items array
        if (this.items.length >= this.firstDoneIndex) {
            const firstDoneInItems = _.findIndex(this.items, it => it.done == true);
            if (firstDoneInItems < 0) {
                this.items.push(item)
            } else {
                this.items.splice(firstDoneInItems, 0, item);
            }
        } else {
            this.list.items.splice(oldIndex, 1);
            this.list.items.splice(this.firstDoneIndex, 0, item);
        }
        if (item.done) {
            this.list.done++;
        } else {
            this.list.done--;
        }
        this.calcFirstDoneIndex();
    }

    private calcFirstDoneIndex() {
        this.firstDoneIndex = (this.list.items.length - this.list.done);
        if (this.firstDoneIndex > this.list.items.length - 1) {
            this.firstDoneIndex--;
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
                this.sendWSAdd();
            } else {
                this.loadItems();
                if (this.refreshPending) {
                    this.sendWSAdd();
                }
            }
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
                this.sendWSRefresh();
            } else {
                this.alertSrv.error("app.item.update.fail");
            }
        })
    }

    /**
     * Update category of passed item
     */
    updateCategory(event) {
        const item = event.item;
        const newCategory = event.category;
        if (newCategory !== item.category) {
            item.category = newCategory;
            this.itemSrv.updateItem(this.listID, item).subscribe(list => {
                if (list) {
                    this.alertSrv.success("app.item.category.updated");
                    this.assignListWithSorting(list);
                    this.sendWSRefresh();
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
        _.remove(this.items, (i) => {
            return i.id === item.id
        });
        this.refreshPending = true;
        this.alertSrv.undoable("app.item.delete.success", {name: item.product.name}).subscribe(undo => {
            if (undo !== undefined && !undo) {
                this.itemSrv.deleteItem(this.listID, item).subscribe((list) => {
                    if (list) {
                        this.sendWSRemove();
                        this.assignListWithSorting(list);
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
                this.sendWSRefresh();
                this.loadItems();
            }
        }, () => {
            this.alertSrv.error('app.shopping.update.fail');
        })
    }

    /**
     * Handler for child element which quickly added some item
     * Main purpose is just to refresh items based on loaded list
     * @param list list to be refreshed
     */
    quickAdd(list: ShoppingList) {
        this.assignListWithSorting(list);
        this.alertSrv.success("app.item.add.success");
        this.sendWSAdd();
    }

    private assignListWithSorting(list: ShoppingList) {
        this.list = list;
        this.sharedCount = list.shared.length;
        this.getSharedButtonTootlip();
        this.calcFirstDoneIndex();
        this.items = this.list.items.slice(0, this.limiter);
    }

    /**
     * When there is scrolling event happening , more favorites should be added and rendered
     * If limiter is greater than there are favorites, just equalize it to length, and stop adding more if it's the end of table
     */
    onScrollDown() {
        // add another 20 items
        const start = this.limiter;
        this.limiter += 20;
        if (this.limiter > this.list.items.length) {
            this.limiter = this.list.items.length
        }
        if (this.limiter > start) {
            this.appendItems(start, this.limiter);
        }
    }

    private appendItems(startIndex, endIndex) {
        for (let i = startIndex; i < endIndex; ++i) {
            this.items.push(this.list.items[i]);
        }
    }

    /**
     * Start edit process if list is not archived
     */
    startEdit() {
        if (!this.list.archived) {
            this.edit = true;
        }
    }

    delayRefresh() {
        this.refreshPending = true;
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
                this.sendWSRefresh();
            }, () => {
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
        this.listSrv.archive(this.list).subscribe(res => {
            if (res) {
                let msgKey = archived ? 'app.shopping.unarchive.success' : 'app.shopping.archive.success';
                this.alertSrv.success(msgKey);
                this.sendWSRefresh();
            }
        }, () => {
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
        this.refreshPending = true;
        this.list.done = 0;
        _.remove(this.list.items, (item) => item.done == true);
        this.items = this.list.items;
        this.alertSrv.undoable("app.shopping.cleanup").subscribe(undo => {
            if (undo !== undefined) {
                if (!undo) {
                    this.listSrv.cleanup(this.listID).subscribe((list) => {
                        if (list) {
                            this.sendWSRemove();
                            this.assignListWithSorting(list);
                        }
                    });
                } else {
                    this.loadItems();
                }
            }
        });
    }

    get percentage() {
        let percentage = this.list.items.length > 0 ? (this.list.done / this.list.items.length) * 100 : 0;
        return parseFloat(`${percentage}`).toFixed(2);
    }

    sendWSRefresh() {
        if (this.stompClient) {
            this.logger.debug(`Sending refresh for list :${this.listID}`);
            this.stompClient.send(environment.ws_send_url + `${this.listID}/refresh`);
            this.refreshPending = false;
        }
    }

    sendWSAdd() {
        if (this.stompClient) {
            this.logger.debug(`Sending add for list :${this.listID}`);
            this.stompClient.send(environment.ws_send_url + `${this.listID}/add`);
        }
    }

    sendWSRemove() {
        if (this.stompClient) {
            this.logger.debug(`Sending remove for list :${this.listID}`);
            this.stompClient.send(environment.ws_send_url + `${this.listID}/remove`);
        }
    }


    initializeWebSocketConnection() {
        let ws = new SockJS(environment.context + environment.ws_ur);
        this.stompClient = Stomp.over(ws);
        this.stompClient.debug = (msg) => {
            this.logger.debug(msg);
        };
        let that = this;
        // noinspection JSUnusedLocalSymbols
        this.stompClient.connect({}, function (frame) {
            that.stompClient.subscribe(`/actions/${that.listID}`, (action) => {
                let wsaction = JSON.parse(action.body) as WSAction;
                if (wsaction.user !== that.currentAccount.id) {
                    switch (wsaction.action) {
                        case WSActionType.ADD:
                            that.loadItems();
                            that.alertSrv.info('app.item.new.one');
                            break;
                        case WSActionType.REFRESH:
                            that.loadItems();
                            break;
                        case WSActionType.REMOVE:
                            that.alertSrv.info('app.item.removed.many');
                            that.loadItems();
                            break;
                    }
                }
            });
        });
    }

    onPull() {
        this.isInProgress = true;
        setTimeout(() => {
            this.loadItems();
        }, 300);
    }

    private copyList() {
        this.listSrv.copyList(this.list).subscribe(result => {
            if (result) {
                this.alertSrv.success('app.shopping.copy.success', {name: this.list.name});
                this.router.navigate(['/list', result.id]);
            }
        }, error => {
            this.alertSrv.error('app,shopping.copy.error');
            this.logger.error(error)
        })
    }

    trackByFn(index, item) {
        return item.id;
    }
}
