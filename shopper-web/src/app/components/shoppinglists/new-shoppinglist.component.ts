import {Component} from '@angular/core';
import {MatDialogRef} from '@angular/material';

@Component({
    templateUrl: './new-shoppinglist.component.html'
})
export class NewShoppingListComponent {

    listName: string;

    constructor(private dialogRef: MatDialogRef<NewShoppingListComponent>) {
    }

    createList() {
        this.dialogRef.close(this.listName);
    }
}