import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatAutocompleteSelectedEvent, MatDialogRef} from '@angular/material';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {Product} from "../../../model/Product";
import {ApiService} from "../../../services/api.service";
import {environment} from "../../../../environments/environment";
import {Category} from "../../../model/Category";
import {TranslateService} from "@ngx-translate/core";
import {ListItem} from "../../../model/ListItem";
import {Observable} from "rxjs";
import * as _ from 'lodash';
import {map, startWith} from "rxjs/operators";
import {CategoryOption} from "../../../model/CategoryOption";

@Component({
    templateUrl: './item-dialog.component.html',
    styleUrls: ['./item-dialog.component.css']
})
export class ItemDialogComponent implements OnInit {

    update: boolean;
    item: ListItem;
    form: FormGroup;
    // products: Product[] = [];
    categories: CategoryOption[];
    filteredCategories: Observable<CategoryOption[]>;
    productTerm: String = '';
    categoryTerm: String = '';

    constructor(private dialogRef: MatDialogRef<ItemDialogComponent>,
                private formBuilder: FormBuilder,
                private api: ApiService,
                private translate: TranslateService,
                @Inject(MAT_DIALOG_DATA) public data: any) {
        //load categories
        this.categories = Object.values(Category).map(value => {
            return {
                category: value,
                name: this.translate.instant(value.toString())
            }
        });
        this.item = data.item ? data.item : new ListItem();
        this.update = data.update;
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

    commitItem() {
        if (this.form.valid) {
            this.dialogRef.close(this.item);
        }
    }

    productDisplay(product?: Product): String | undefined {
        return product ? product.name : undefined;
    }

    ngOnInit() {
    }

    private _filter(value: string): CategoryOption[] {
        const filterValue = value.toLowerCase();
        return value ? _.filter(this.categories, cat => cat.name.toLowerCase().includes(filterValue)) : this.categories;
    }

    setProduct(event: MatAutocompleteSelectedEvent) {
        let product = event.option.value;
        this.item.product = product;
        this.form.controls.category.setValue(product.topCategory);
    }
}