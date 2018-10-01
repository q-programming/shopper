import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material';
import {FormControl, FormGroup, Validators} from "@angular/forms";
import {CategoryPreset} from "../../../model/CategoryPreset";
import {ShoppingList} from "../../../model/ShoppingList";
import * as _ from 'lodash';

@Component({
    templateUrl: './shopping-list-dialog.component.html'
})
export class ShoppingListDialogComponent implements OnInit {

    form: FormGroup;
    presets: CategoryPreset[];
    selectedPreset: CategoryPreset;
    list: ShoppingList;
    update: boolean;

    constructor(private dialogRef: MatDialogRef<ShoppingListDialogComponent>,
                @Inject(MAT_DIALOG_DATA) public data: any) {
        let defaultPreset = new CategoryPreset();
        this.presets = (data.presets as CategoryPreset[]);
        this.presets.unshift(defaultPreset);
        this.list = data.list;
        if (this.list && this.list.preset && !_.find(this.presets, (p) => p.id == this.list.preset.id)) {
            this.presets.push(this.list.preset);
        }
        this.selectedPreset = (this.list && this.list.preset) ? this.list.preset : defaultPreset;
        this.update = data.update;
    }

    commitList() {
        if (this.form.valid) {
            this.dialogRef.close({
                listName: this.form.value.listName,
                preset: this.form.value.preset
            });
        }
    }

    ngOnInit() {
        this.form = new FormGroup({
            listName: new FormControl(this.list ? this.list.name : '', Validators.required),
            preset: new FormControl(this.selectedPreset, Validators.required)
        })
    }

    comparePresets(o1: CategoryPreset, o2: CategoryPreset) {
        return o1.id == o2.id
    }
}