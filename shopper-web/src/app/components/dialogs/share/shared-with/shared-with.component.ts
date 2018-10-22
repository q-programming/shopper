import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {AccountService} from "@services/account.service";
import {Account} from "@model/Account";
import {AuthenticationService} from "@services/authentication.service";
import * as _ from 'lodash';
import {ApiService} from "@services/api.service";
import {AlertService} from "@services/alert.service";
import {ShoppingList} from "@model/ShoppingList";
import {environment} from "@env/environment";
import {NGXLogger} from "ngx-logger";

@Component({
    selector: 'shared-with',
    templateUrl: './shared-with.component.html',
    styles: []
})
export class SharedWithComponent implements OnInit {

    @Input() list: ShoppingList;
    @Output() done = new EventEmitter<ShoppingList>();
    sharedWith: Account[];
    owner: Account;


    constructor(private logger: NGXLogger,
                private accountSrv: AccountService,
                private authSrv: AuthenticationService,
                private api: ApiService,
                private alertSrv: AlertService) {
    }

    ngOnInit() {
        let currentID = this.authSrv.currentAccount.id;
        if (this.list.ownerId !== currentID) {
            this.list.shared.push(this.list.ownerId);
        }
        _.pull(this.list.shared, currentID);
        if (this.list.shared.length > 0) {
            this.accountSrv.getUsers(this.list.shared).then(users => {
                this.sharedWith = users;
            });
        }
    }

    stopSharing(account: Account) {
        this.api.postObject<ShoppingList>(environment.list_url + `/${this.list.id}/stop-sharing`, account.id)
            .subscribe(list => {
                if (list) {
                    this.alertSrv.success('app.shopping.share.stop.success', {
                        name: account.fullname
                    });
                    _.remove(this.sharedWith, (acc) => acc.id == account.id)
                    this.done.emit(list);
                }
            }, error => {
                this.alertSrv.error('app.shopping.share.stop.fail');
                this.logger.error(error)
            });
    }
}
