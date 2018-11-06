import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {FormGroup} from "@angular/forms";
import {ApiService} from "@services/api.service";
import {ItemService} from "@services/item.service";
import {ShoppingList} from "@model/ShoppingList";

@Component({
    selector: 'app-quickadd',
    templateUrl: './quickadd.component.html',
    styles: []
})
export class QuickaddComponent implements OnInit {

    @Input()
    listID: number;
    @Input()
    demo: boolean;
    @Output()
    created = new EventEmitter<ShoppingList>();
    form: FormGroup;
    productName: string;


    constructor(private api: ApiService, private itemSrv: ItemService) {
    }

    ngOnInit() {
    }

    createItem() {
        if (this.productName && !this.demo) {
            this.itemSrv.createNewItem(this.listID, {product: {name: this.productName}}).subscribe(list => {
                if (list) {
                    this.productName = '';
                    this.created.emit(list);
                }
            });
        }
    }
}
