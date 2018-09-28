import {Component, OnInit, ViewChild} from '@angular/core';
import {AlertService} from "../../../services/alert.service";
import {ApiService} from "../../../services/api.service";
import {Category} from "../../../model/Category";
import {CategoryPreset} from "../../../model/CategoryPreset";
import {FormControl, Validators} from "@angular/forms";
import {ListService} from "../../../services/list.service";
import * as _ from "lodash"

@Component({
    selector: 'settings-products',
    templateUrl: './products-settings.component.html',
    styleUrls: ["./products-settings.component.css"]
})
export class ProductsSettingsComponent implements OnInit {
    @ViewChild('orderingName') orderingNameInput;
    categoryPresetControl: FormControl;
    presets: CategoryPreset[];
    preset: CategoryPreset;
    defaultOrdering: Category[];
    currentOrdering: Category[];

    constructor(private alertSrv: AlertService, private api: ApiService, private listSrv: ListService) {
        this.categoryPresetControl = new FormControl('', [Validators.required]);
        this.listSrv.getDefaultCategoriesSorting().subscribe(defaults => {
            if (defaults) {
                this.defaultOrdering = defaults[0].split(",").map(value => Category[value]);
            }
            else {
                this.defaultOrdering = Object.values(Category);
            }
        })
    }

    ngOnInit() {
        this.loadUserPresets();
    }

    private loadUserPresets() {
        this.listSrv.loadUserSortingPresets().subscribe(result => {
            this.presets = result;
            this.selectFirstOrCurrentPreset();
        })
    }

    private selectFirstOrCurrentPreset() {
        if (this.presets.length > 0) {
            if (!this.preset) {
                this.preset = this.presets[0];
            } else {
                this.preset = _.find(this.presets, (p) => p.id == this.preset.id);
            }
            this.loadCategoryOrder();
        }
    }

    loadCategoryOrder() {
        this.currentOrdering = this.preset.categoriesOrder.split(",").map(value => Category[value]);
        this.categoryPresetControl.setValue(this.preset.name);
    }

    createNewCategoryPreset() {
        this.preset = new CategoryPreset();
        this.currentOrdering = this.defaultOrdering;
        this.categoryPresetControl.setValue(this.preset.name);
        setTimeout(() => {
            this.orderingNameInput.nativeElement.focus()
        }, 0)

    }

    deleteCategoryPreset() {
        let trashedPreset = this.preset;
        if (trashedPreset && trashedPreset.id) {
            _.remove(this.presets, (i) => {
                return i.id === trashedPreset.id
            });
            this.preset = undefined;
            this.selectFirstOrCurrentPreset();
            this.alertSrv.undoable('app.settings.products.category.delete.success').subscribe(undo => {
                if (undo !== undefined) {
                    if (undo) {
                        this.loadUserPresets();
                    } else {
                        this.listSrv.deleteCategoryPreset(trashedPreset).subscribe(() => {
                        }, () => {
                            this.alertSrv.error('app.settings.products.category.delete.fail');
                        })
                    }
                }

            })
        }
    }

    saveCategoryPreset() {
        if (this.categoryPresetControl.valid) {
            this.preset.name = this.categoryPresetControl.value;
            this.preset.categoriesOrder = this.currentOrdering.join(",");
            this.listSrv.saveCategoryPreset(this.preset).subscribe(result => {
                if (result) {
                    this.alertSrv.success('app.settings.products.category.success', {name: this.preset.name});
                    this.preset = result;
                    this.loadUserPresets();
                }
            })
        }
    }
}
