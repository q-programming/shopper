import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material';
import {ListItem} from "../../../model/ListItem";
import {CategoryOption} from "../../../model/CategoryOption";

@Component({
    templateUrl: './item-dialog.component.html',
    styleUrls: ['./item-dialog.component.css']
})
export class ItemDialogComponent implements OnInit {

    update: boolean;
    listID: number;
    item: ListItem;
    categories: CategoryOption[];


    constructor(private dialogRef: MatDialogRef<ItemDialogComponent>,
                @Inject(MAT_DIALOG_DATA) public data: any) {
        //load categories
        this.categories = data.categories;
        this.item = data.item ? data.item : new ListItem();
        this.update = data.update;
        this.listID = data.listID;
    }


    commitItem(valid: boolean) {
        if (valid) {
            this.dialogRef.close(this.item);
        }
    }

    ngOnInit() {
    }

}