import {Component, Input, OnInit} from '@angular/core';
import {ApiService} from "../../../../services/api.service";
import {Product} from "../../../../model/Product";
import {environment} from "../../../../../environments/environment";
import * as _ from "lodash"
import {AlertService} from "../../../../services/alert.service";
import {ShoppingList} from "../../../../model/ShoppingList";
import {ListItem} from "../../../../model/ListItem";
import {Observable} from "rxjs";
import {FormControl} from "@angular/forms";
import {map, startWith} from "rxjs/operators";

@Component({
    selector: 'item-favorites',
    templateUrl: './favorites.component.html',
    styleUrls: ["./favorites.component.css"]
})
export class FavoritesComponent implements OnInit {

    filteredProducts: Observable<Product[]>;
    favorites: Product[] = [];
    @Input() listID: number;
    filterControl: FormControl;
    filter: string;

    constructor(private api: ApiService, private alertSrv: AlertService) {
    }

    ngOnInit() {
        this.filterControl = new FormControl();
        this.api.getObject<Product>(environment.item_url + `/${this.listID}/favorites`)
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

    addItem(product: Product) {
        let item = new ListItem();
        item.product = product;
        this.filterControl.setValue('');
        _.remove(this.favorites, (p) => p.id === product.id);
        this.api.postObject<ShoppingList>(environment.item_url + `/${this.listID}/add`, item).subscribe(result => {
            if (result) {
                this.alertSrv.success("app.item.add.named.success", {name: product.name});
            }
        })
    }

    private _filter(value: string): Product[] {
        const filterValue = value.toLowerCase();
        return value ? _.filter(this.favorites, prod => prod.name.toLowerCase().includes(filterValue)) : this.favorites;
    }

}
