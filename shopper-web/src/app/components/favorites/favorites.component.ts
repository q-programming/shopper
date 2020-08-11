import {Component, HostListener, Input, OnInit} from '@angular/core';
import {ApiService} from "@services/api.service";
import {Product} from "@model/Product";
import {environment} from "@env/environment";
import * as _ from "lodash"
import {AlertService} from "@services/alert.service";
import {ShoppingList} from "@model/ShoppingList";
import {ListItem} from "@model/ListItem";
import {Observable} from "rxjs";
import {FormControl} from "@angular/forms";
import {map} from "rxjs/operators";
import {MenuAction, MenuActionsService} from "@services/menu-actions.service";

@Component({
    selector: 'item-favorites',
    templateUrl: './favorites.component.html',
    styleUrls: ["./favorites.component.css"]
})
export class FavoritesComponent implements OnInit {

    filteredProducts: Observable<Product[]>;
    favorites: Product[] = [];
    limitedFavorites: Product[] = [];
    @Input() listID: number;
    @Input() settings: boolean;
    filterControl: FormControl;
    filter: string;
    limiter;
    public innerHeight: any;

    SM_LIMIT = 40;
    MD_LIMIT = 60;
    XL_LIMIT = 80;

    constructor(private api: ApiService, private alertSrv: AlertService, private menuSrv: MenuActionsService,) {
    }

    ngOnInit() {
        this.innerHeight = window.innerHeight;
        this.limiter = this.initialLimiter();
        this.filterControl = new FormControl();
        this.fetchFavorites();
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
    onResize(event) {
        this.innerHeight = window.innerHeight;
        const newLimiter = this.initialLimiter();
        if (newLimiter != this.limiter) {
            this.limiter = newLimiter;
            this.limitedFavorites = this.favorites.slice(0, this.limiter);
            this.initFilteredFavorites();
        }
    }

    private fetchFavorites() {
        let url = environment.item_url;
        if (this.listID && !this.settings) {
            url += `/favorites/list/${this.listID}`;
        } else {
            url += `/favorites`;
        }
        this.api.getObject<Product[]>(url)
            .subscribe(response => {
                this.favorites = response;
                this.limitedFavorites = this.favorites.slice(0, this.limiter);
                this.initFilteredFavorites();
            });
    }

    private initFilteredFavorites() {
        this.filteredProducts = this.filterControl.valueChanges
            .distinctUntilChanged()
            .startWith('')
            .pipe(
                map(value => {
                    this.filter = value;
                    return this._filter(value);
                }));
    }

    /**
     * Perform operation on product if favorite component is within settings,
     * it's remove from favorites, otherwise it's addition of item
     * @param product Product to be operated on
     */
    operation(product: Product) {
        if (this.settings) {
            this.removeFavorite(product);
        } else {
            this.addFavoriteProductToList(product)
        }
    }

    private removeFavorite(product: Product) {
        _.remove(this.favorites, (p) => p.id === product.id);
        _.remove(this.limitedFavorites, (p) => p.id === product.id);
        this.alertSrv.undoable("app.item.remove.favorites.success", {name: product.name}).subscribe(undo => {
            if (undo !== undefined) {
                if (undo) {
                    this.fetchFavorites();
                } else {
                    this.api.postObject(environment.item_url + `/favorites/remove`, product).subscribe(() => {
                        this.fetchFavorites();
                    });
                }
            }
        })
    }

    private addFavoriteProductToList(product: Product) {
        let item = new ListItem();
        item.product = product;
        this.filterControl.setValue('');
        _.remove(this.limitedFavorites, (p) => p.id === product.id);
        this.api.postObject<ShoppingList>(environment.item_url + `/${this.listID}/add`, item).subscribe(result => {
            if (result) {
                this.alertSrv.success("app.item.add.named.success", {name: product.name});
                this.menuSrv.emmitAction(MenuAction.PENDING_REFRESH);
            }
        })
    }

    private _filter(value: string): Product[] {
        const filterValue = value.toLowerCase();
        return value ? _.filter(this.favorites, prod => prod.name.toLowerCase().includes(filterValue)) : this.limitedFavorites;
    }

    trackByFn(index, item) {
        return item.id;
    }

    /**
     * When there is scrolling event happening , more favorites should be added and rendered
     * If limiter is greater than there are favorites, just equalize it to length, and stop adding more if it's the end of table
     */
    onScrollDown() {
        // add another 20 items
        const start = this.limiter;
        this.limiter += 20;
        if (this.limiter > this.favorites.length) {
            this.limiter = this.favorites.length
        }
        if (this.limiter > start) {
            this.appendItems(start, this.limiter);
        }
    }

    private appendItems(startIndex, endIndex) {
        this.addMoreProducts(startIndex, endIndex);
    }

    private addMoreProducts(startIndex, endIndex) {
        for (let i = startIndex; i < endIndex; ++i) {
            this.limitedFavorites.push(this.favorites[i]);
        }
    }

}
