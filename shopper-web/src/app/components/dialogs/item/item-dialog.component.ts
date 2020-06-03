import {Component, Inject, OnInit} from '@angular/core';
import {ListItem} from "@model/ListItem";
import {CategoryOption} from "@model/CategoryOption";
import {MenuAction, MenuActionsService} from "@services/menu-actions.service";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";

@Component({
    templateUrl: './item-dialog.component.html',
    styleUrls: ['./item-dialog.component.css']
})
export class ItemDialogComponent implements OnInit {

    update: boolean;
    listID: number;
    item: ListItem;
    categories: CategoryOption[];
    favorites: string[];
    formValid: boolean;
    toTopVisible:boolean;


    constructor(private dialogRef: MatDialogRef<ItemDialogComponent>,
                @Inject(MAT_DIALOG_DATA) public data: any,
                private menuSrv: MenuActionsService) {
        //load categories
        this.categories = data.categories;
        this.favorites = data.favorites;
        this.item = data.item ? data.item : new ListItem();
        this.update = data.update;
        this.listID = data.listID;
        dialogRef.keydownEvents().subscribe(e => {
            if (e.code === 'Escape') {
                dialogRef.close();
            } else if (e.code === 'Enter' && this.formValid) {
                const that = this;
                setTimeout(function () {
                    const active = document.activeElement;
                    if (active.id !== 'productInput') {
                        that.commitItem(that.formValid);
                    }
                });
            }
        });
    }


    commitItem(valid: boolean) {
        if (valid) {
            this.dialogRef.close(this.item);
        }
    }

    emitCommit() {
        this.menuSrv.emmitAction(MenuAction.COMMIT);
    }

    ngOnInit() {
    }
}