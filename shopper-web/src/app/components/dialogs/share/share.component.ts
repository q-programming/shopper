import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material";
import {ShoppingList} from "../../../model/ShoppingList";
import {FormControl, Validators} from "@angular/forms";

@Component({
    selector: 'app-share',
    templateUrl: './share.component.html',
    styles: []
})
export class ShareComponent implements OnInit {
    list: ShoppingList;
    emailControl: FormControl;

    constructor(private dialogRef: MatDialogRef<ShareComponent>,
                @Inject(MAT_DIALOG_DATA) public data: ShoppingList) {
        this.list = data;
        this.emailControl = new FormControl('', [Validators.required, Validators.email])
    }

    ngOnInit() {
    }

    shareList() {
        if (this.emailControl.valid) {
            this.dialogRef.close(this.emailControl.value);
        }
    }

}
