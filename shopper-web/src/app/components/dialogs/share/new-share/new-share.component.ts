import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {FormControl, Validators} from "@angular/forms";
import {ShoppingList} from "@model/ShoppingList";
import {environment} from "@env/environment";
import {ApiService} from "@services/api.service";
import {AlertService} from "@services/alert.service";
import {ShareComponent} from "../share.component";
import {Account} from "@model/Account";
import {debounceTime, finalize, switchMap, tap} from 'rxjs/operators';
import {Observable} from "rxjs";
import {MatDialogRef} from "@angular/material/dialog";

@Component({
    selector: 'new-share',
    templateUrl: './new-share.component.html',
    styleUrls: ['./new-share.component.css']
})
export class NewShareComponent implements OnInit {

    emailControl: FormControl;
    filteredAccounts: Account[];
    isLoading = false;
    term: string;
    @Input() list: ShoppingList;
    @Input() dialogRef: MatDialogRef<ShareComponent>;
    @Output()
    done = new EventEmitter<Boolean>();

    constructor(private api: ApiService, private alertSrv: AlertService) {
        this.emailControl = new FormControl('', [Validators.required, Validators.email])
    }

    ngOnInit() {
        this.emailControl
            .valueChanges
            .pipe(
                debounceTime(300),
                tap(() => this.isLoading = true),
                switchMap(value => this.getFriendList(value)
                    .pipe(
                        finalize(() => this.isLoading = false),
                    )
                )
            )
            .subscribe(users => this.filteredAccounts = users);

    }

    shareList() {
        if (this.emailControl.valid) {
            let email = this.emailControl.value;
            // this.alertSrv.info('app.shopping.share.email.inqueue');
            this.api.postObject<ShoppingList>(environment.list_url + `/${this.list.id}/share`, email)
                .subscribe(shared => {
                    if (shared) {
                        this.alertSrv.success('app.shopping.share.sent', {
                            name: this.list.name,
                            email: email
                        });
                    }
                });
            this.emailControl.setValue(undefined);
            this.dialogRef.close(true);
            this.done.emit(true);
        }
    }

    getFriendList(value: string): Observable<Account[]> {
        this.term = value;
        return this.api.get(`${environment.account_url}/friends`, {term: value})
    }

    displayFn(email: string) {
        if (email) {
            return email;
        }
    }


}
