import {Component, Input, OnInit} from '@angular/core';
import {ApiService} from "../../../../services/api.service";
import {Product} from "../../../../model/Product";
import {environment} from "../../../../../environments/environment";
import * as _ from "lodash"
import {AlertService} from "../../../../services/alert.service";
import {ShoppingList} from "../../../../model/ShoppingList";
import {ListItem} from "../../../../model/ListItem";

@Component({
    selector: 'item-favorites',
    templateUrl: './favorites.component.html',
    styleUrls: ["./favorites.component.css"]
})
export class FavoritesComponent implements OnInit {

    favorites: Product[];
    @Input()
    listID: number;

    constructor(private api: ApiService, private alertSrv: AlertService) {
    }

    ngOnInit() {
        this.api.getObject<Product>(environment.item_url + `/${this.listID}/favorites`)
            .subscribe(response => {
                this.favorites = response;
            });
    }

    addItem(product: Product) {
        let item = new ListItem();
        item.product = product;
        _.remove(this.favorites, (p) => p.id === product.id);
        this.api.postObject<ShoppingList>(environment.item_url + `/${this.listID}/add`, item).subscribe(result => {
            if (result) {
                this.alertSrv.success("app.item.add.named.success", {name: product.name});
            }
        })
    }

}
