import {Component, Inject, OnInit} from '@angular/core';

import {FormControl, FormGroup, Validators} from "@angular/forms";
import {CategoryPreset} from "@model/CategoryPreset";
import {ShoppingList} from "@model/ShoppingList";
import * as _ from 'lodash';
import {Account} from "@model/Account";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";

@Component({
    templateUrl: './shopping-list-dialog.component.html'
})
export class ShoppingListDialogComponent implements OnInit {

    form: FormGroup;
    presets: CategoryPreset[];
    selectedPreset: CategoryPreset;
    list: ShoppingList;
    update: boolean;
    currentAccount: Account;

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
        this.currentAccount = data.currentAccount;
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