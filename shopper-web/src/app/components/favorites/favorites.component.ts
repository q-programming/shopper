import {Component, Input, OnInit} from '@angular/core';
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
    @Input() listID: number;
    @Input() settings: boolean;
    filterControl: FormControl;
    filter: string;

    constructor(private api: ApiService, private alertSrv: AlertService, private menuSrv: MenuActionsService,) {
    }

    ngOnInit() {
        this.filterControl = new FormControl();
        this.fetchFavorites();

    }

    private fetchFavorites() {
        let url = environment.item_url;
        if (this.listID && !this.settings) {
            url += `/favorites/list/${this.listID}`;
        } else {
            url += `/favorites`;
        }
        this.api.getObject<Product>(url)
            .subscribe(response => {
                this.favorites = response;
                this.filteredProducts = this.filterControl.valueChanges
                    .distinctUntilChanged()
                    .startWith('')
                    .pipe(
                        map(value => {
                            this.filter = value;
                            return this._filter(value);
                        }));
            });
    }

    operation(product: Product) {
        if (this.settings) {
            this.removeFavorite(product);
        } else {
            this.addItem(product)
        }
    }

    private removeFavorite(product: Product) {
        _.remove(this.favorites, (p) => p.id === product.id);
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

    private addItem(product: Product) {
        let item = new ListItem();
        item.product = product;
        this.filterControl.setValue('');
        _.remove(this.favorites, (p) => p.id === product.id);
        this.api.postObject<ShoppingList>(environment.item_url + `/${this.listID}/add`, item).subscribe(result => {
            if (result) {
                this.alertSrv.success("app.item.add.named.success", {name: product.name});
                this.menuSrv.emmitAction(MenuAction.PENDING_REFRESH);
            }
        })
    }

    private _filter(value: string): Product[] {
        const filterValue = value.toLowerCase();
        return value ? _.filter(this.favorites, prod => prod.name.toLowerCase().includes(filterValue)) : this.favorites;
    }

}