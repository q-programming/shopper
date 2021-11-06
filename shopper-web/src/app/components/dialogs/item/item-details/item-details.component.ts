import {Component, EventEmitter, Input, OnDestroy, OnInit, Output} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {debounceTime, distinctUntilChanged, map, startWith} from "rxjs/operators";
import {ApiService} from "@services/api.service";
import {ListItem} from "@model/ListItem";
import {CategoryOption} from "@model/CategoryOption";
import {Observable, Subscription} from "rxjs";
import {Product} from "@model/Product";
import {environment} from "@env/environment";
import * as _ from 'lodash';
import {itemDisplayName} from "../../../../utils/utils";
import {MenuAction, MenuActionsService} from "@services/menu-actions.service";

@Component({
    selector: 'item-details',
    templateUrl: './item-details.component.html',
    styles: []
})
export class ItemDetailsComponent implements OnInit, OnDestroy {

    form: FormGroup;
    @Input() item: ListItem;
    @Input() listID: number;
    @Input() update: boolean;
    @Output() itemChange: EventEmitter<ListItem> = new EventEmitter<ListItem>();
    @Input() categories: CategoryOption[];
    @Input() favorites: string[];
    @Output() commit: EventEmitter<boolean> = new EventEmitter<boolean>();
    @Output() valid: EventEmitter<boolean> = new EventEmitter<boolean>();
    formValid: boolean;
    filteredCategories: Observable<CategoryOption[]>;
    filteredFavorites: Observable<string[]>;
    productTerm: String = '';
    categoryTerm: String = '';
    menuSub: Subscription;


    constructor(private formBuilder: FormBuilder,
                private api: ApiService, private menuSrv: MenuActionsService) {
    }

    ngOnInit() {
        if (!this.item) {
            this.item = new ListItem();
        }
        this.form = this.formBuilder.group({
            product: [this.item.product ? itemDisplayName(this.item) : '', Validators.required],
            category: [this.item.category, Validators.required],
            quantity: this.item.quantity > 0 ? this.item.quantity : 1,
            unit: this.item.unit,
            description: this.item.description,
            categoryFilterCtrl: '',
            productFilterCtrl: ''
        });
        //filtered product
        this.filteredFavorites = this.form.controls.product.valueChanges
            .pipe(
                debounceTime(600),
                distinctUntilChanged(),
                startWith<string>(''),
                map(value => {
                        if (typeof value === "string" && value.length >= 1) {
                            this.productTerm = value;
                            this.handleProductValueChange(value);
                            return this._filterProducts(value)
                        } else {
                            return [];
                        }
                    }
                ));
        //filtered categories
        this.filteredCategories = this.form.controls.categoryFilterCtrl.valueChanges
            .pipe(
                startWith(''),
                map(value => {
                    if (typeof value === "string") {
                        this.categoryTerm = value;
                        return this._filter(value);
                    }
                }));
        this.form.controls.category.valueChanges.subscribe(value => this.item.category = value);
        this.form.controls.unit.valueChanges.subscribe(value => this.item.unit = value);
        this.form.controls.quantity.valueChanges.subscribe(value => this.item.quantity = value);
        this.form.controls.description.valueChanges.subscribe(value => this.item.description = value);
        this.form.valueChanges.subscribe(() => {
            if (this.formValid !== this.form.valid) {
                this.formValid = this.form.valid;
                this.valid.emit(this.formValid);
            }
        });
        this.menuSub = this.menuSrv.actionEmitted.subscribe(action => {
            if (action === MenuAction.COMMIT) {
                this.commitItem()
            }
        })
    }

    ngOnDestroy() {
        this.menuSub.unsubscribe();
    }

    private handleProductValueChange(value) {
        if (typeof value === 'string') {
            this.item.product = {name: value};
            this.api.getObject<Product>(environment.product_url + '/category', {term: value})
                .subscribe(response => {
                    if (response) {
                        this.form.controls.category.setValue(response);
                        this.item.product = new Product(value);
                    }
                })
        }
    }


    private _filter(value: string): CategoryOption[] {
        const filterValue = value.toLowerCase();
        return value ? _.filter(this.categories, cat => cat.name.toLowerCase().includes(filterValue)) : this.categories;
    }

    private _filterProducts(value: string): string[] {
        const filterValue = value.toLowerCase();
        return value ? _.filter(this.favorites, product => product.toLowerCase().includes(filterValue)) : this.favorites;
    }


    commitItem() {
        if (this.form.valid) {
            this.commit.emit(this.form.value)
        }
    }

    tryToGetQuantity(): string {
        this.productTerm = '';
        let result = <string>this.form.controls.product.value;
        let parts = result.split(' ').filter(i => i);
        const wordCounts = parts.length;
        if (wordCounts > 1) {
            let b = parts[0].replace(',', '.');
            let e = parts[wordCounts - 1].replace(',', '.');
            if (this.isQuantityAndUnit(b) && !this.isQuantityAndUnit(e)) {
                this.setQuantityAndUnit(b);
                result = parts.slice(1).join(" ");
                this.form.controls.product.setValue(result)
            } else if (this.isQuantityAndUnit(e)) {
                this.setQuantityAndUnit(e);
                result = parts.slice(0, wordCounts - 1).join(" ");
                this.form.controls.product.setValue(result)
            }
        }
        return result;
    }

    private isQuantityAndUnit(part: string): boolean {
        return /^(\d+(\.|,?)\d*)(kg|g|l|m|cm|ml|dkg)?$/.test(part);
    }

    private setQuantityAndUnit(quantityAndUnit: string) {
        const split = quantityAndUnit.split(/(kg|g|l|m|cm|ml|dkg)/);
        const unit = quantityAndUnit.replace(split[0], "");
        this.form.controls.quantity.setValue(Number(split[0]));
        this.form.controls.unit.setValue(unit);
    }
}
