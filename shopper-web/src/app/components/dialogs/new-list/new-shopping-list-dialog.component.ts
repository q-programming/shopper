import {Component, OnInit} from '@angular/core';
import {MatDialogRef} from '@angular/material';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";

@Component({
    templateUrl: './new-shopping-list-dialog.component.html'
})
export class NewShoppingListDialogComponent implements OnInit {

    form: FormGroup;

    constructor(private dialogRef: MatDialogRef<NewShoppingListDialogComponent>,
                private formBuilder: FormBuilder) {
    }

    createList() {
        if (this.form.valid) {
            this.dialogRef.close(this.form.value.listName);
        }
    }

    ngOnInit() {
        this.form = this.formBuilder.group({
            listName: ['', Validators.required]
        })
    }
}