import {Component, OnInit} from '@angular/core';
import {AlertService} from "../../../services/alert.service";
import {ApiService} from "../../../services/api.service";
import {Category} from "../../../model/Category";
import {CategoryPreset} from "../../../model/CategoryPreset";
import {FormControl, Validators} from "@angular/forms";
import {ListService} from "../../../services/list.service";

@Component({
    selector: 'settings-products',
    templateUrl: './products-settings.component.html',
    styleUrls: ["./products-settings.component.css"]
})
export class ProductsSettingsComponent implements OnInit {

    categoryPresetControl: FormControl;
    presets: CategoryPreset[];
    preset: CategoryPreset;
    defaultOrdering: Category[] = Object.values(Category);
    currentOrdering: Category[];

    constructor(private alertSrv: AlertService, private api: ApiService, private listSrv: ListService) {
        this.categoryPresetControl = new FormControl('', [Validators.required])
    }

    ngOnInit() {
        this.loadUserPresets();
    }

    private loadUserPresets() {
        this.listSrv.loadUserSortingPresets().subscribe(result => {
            this.presets = result;
            if (this.presets.length > 0) {
                this.preset = this.presets[0];
                this.loadCategoryOrder();
            }
        })
    }

    loadCategoryOrder() {
        this.currentOrdering = this.preset.categoriesOrder.split(",").map(value => Category[value]);
        this.categoryPresetControl.setValue(this.preset.name);
    }

    createNewCategoryPreset() {
        this.preset = new CategoryPreset();
        this.currentOrdering = this.defaultOrdering;
        this.categoryPresetControl.setValue(this.preset.name);
    }

    saveCategoryPreset() {
        if (this.categoryPresetControl.valid) {
            this.preset.name = this.categoryPresetControl.value;
            this.preset.categoriesOrder = this.currentOrdering.join(",");
            this.listSrv.saveCategoryPreset(this.preset).subscribe(result => {
                if (result) {
                    this.alertSrv.success('app.settings.products.category.success', {name: this.preset.name})
                    this.loadUserPresets();
                }
            })
        }
    }
}
