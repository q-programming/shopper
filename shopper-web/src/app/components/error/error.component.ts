import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from "@angular/router";

@Component({
    selector: 'app-error',
    templateUrl: './error.component.html',
    styles: []
})
export class ErrorComponent implements OnInit {

    type: string;
    message: string;

    constructor(private activatedRoute: ActivatedRoute) {
    }

    ngOnInit() {
        this.activatedRoute.queryParams.subscribe(params => {
            this.type = params['type'];
            switch (this.type) {
                case 'account':
                    this.message = 'app.error.token.invalid';
                    break;
                case 'expired':
                    this.message = 'app.error.token.expired';
                    break;
                case '404':
                    this.message = 'app.error.404';
                    break;
            }
        });
    }

}
