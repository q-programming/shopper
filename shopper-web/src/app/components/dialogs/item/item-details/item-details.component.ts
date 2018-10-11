import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {map, startWith} from "rxjs/operators";
import {ApiService} from "../../../../services/api.service";
import {ListItem} from "../../../../model/ListItem";
import {CategoryOption} from "../../../../model/CategoryOption";
import {Observable} from "rxjs";
import {Product} from "../../../../model/Product";
import {environment} from "../../../../../environments/environment";
import * as _ from 'lodash';

@Component({
    selector: 'item-details',
    templateUrl: './item-details.component.html',
    styles: []
})
export class ItemDetailsComponent implements OnInit {

    form: FormGroup;
    @Input() item: ListItem;
    @Input() listID: number;
    @Input() update: boolean;
    @Output() itemChange: EventEmitter<ListItem> = new EventEmitter<ListItem>();
    @Input() categories: CategoryOption[];
    @Output() commit: EventEmitter<boolean> = new EventEmitter<boolean>();
    filteredCategories: Observable<CategoryOption[]>;
    productTerm: String = '';
    categoryTerm: String = '';


    constructor(private formBuilder: FormBuilder,
                private api: ApiService) {
    }

    ngOnInit() {
        if (!this.item) {
            this.item = new ListItem();
        }
        this.form = this.formBuilder.group({
            product: [this.item.product ? this.item.product.name : '', Validators.required],
            category: [this.item.category, Validators.required],
            quantity: this.item.quantity,
            unit: this.item.unit,
            description: this.item.description,
            categoryFilterCtrl: ''
        });
        //product
        this.form.controls.product.valueChanges
            .debounceTime(400)
            .distinctUntilChanged()
            .subscribe(value => {
                this.productTerm = value;
                this.handleProductValueChange(value);
            });
        //filtered categories
        this.filteredCategories = this.form.controls.categoryFilterCtrl.valueChanges
            .pipe(
                startWith(''),
                map(value => {
                    this.categoryTerm = value;
                    return this._filter(value);
                }));
        this.form.controls.category.valueChanges.subscribe(value => this.item.category = value);
        this.form.controls.unit.valueChanges.subscribe(value => this.item.unit = value);
        this.form.controls.quantity.valueChanges.subscribe(value => this.item.quantity = value);
        this.form.controls.description.valueChanges.subscribe(value => this.item.description = value);
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

    commitItem() {
        if (this.form.valid) {
            this.commit.emit(this.form.value)
        }
    }


}
