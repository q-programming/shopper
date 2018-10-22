import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material";
import {ShoppingList} from "@model/ShoppingList";

@Component({
    selector: 'app-share',
    templateUrl: './share.component.html',
    styles: []
})
export class ShareComponent implements OnInit {
    public shoppingList: ShoppingList;

    constructor(public dialogRef: MatDialogRef<ShareComponent>,
                @Inject(MAT_DIALOG_DATA) public data: ShoppingList) {
        this.shoppingList = data;
    }

    ngOnInit() {
    }

    shared(done: Boolean) {
        if (done) {
            this.dialogRef.close(done);
        }
    }

    stopedSharing(list: ShoppingList) {
        if (list) {
            this.shoppingList = list;
        }
    }

    close() {
        this.dialogRef.close(true);
    }

}
