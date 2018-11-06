import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from "@angular/router";

@Component({
    selector: 'app-confirm',
    templateUrl: './success.component.html',
    styleUrls: ['success.component.css']
})
export class SuccessComponent implements OnInit {
    message: string;
    type: string;

    constructor(private activatedRoute: ActivatedRoute) {
    }

    ngOnInit() {
        this.activatedRoute.queryParams.subscribe(params => {
            this.type = params['type'];
            switch (this.type) {
                case 'confirmed':
                    this.message = 'app.success.confirmed';
                    break;


            }
        });
    }

}
