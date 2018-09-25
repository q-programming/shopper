import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {FormControl, Validators} from "@angular/forms";
import {ShoppingList} from "../../../../model/ShoppingList";
import {environment} from "../../../../../environments/environment";
import {ApiService} from "../../../../services/api.service";
import {AlertService} from "../../../../services/alert.service";

@Component({
    selector: 'new-share',
    templateUrl: './new-share.component.html',
    styles: []
})
export class NewShareComponent implements OnInit {

    emailControl: FormControl;
    @Input() list: ShoppingList;
    @Output()
    done = new EventEmitter<Boolean>();

    constructor(private api: ApiService, private alertSrv: AlertService) {
        this.emailControl = new FormControl('', [Validators.required, Validators.email])
    }

    ngOnInit() {
    }

    shareList() {
        if (this.emailControl.valid) {
            this.alertSrv.info('app.shopping.share.email.inqueue');
            this.api.postObject<ShoppingList>(environment.list_url + `/${this.list.id}/share`, this.emailControl.value)
                .subscribe(shared => {
                    if (shared) {
                        this.alertSrv.success('app.shopping.share.sent', {
                            name: this.list.name,
                            email: this.emailControl.value
                        });
                    }
                });
            this.done.emit(true);
        }
    }


}
