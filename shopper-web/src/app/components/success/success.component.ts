import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from "@angular/router";
import {environment} from "@env/environment";
import {TranslateService} from "@ngx-translate/core";
import {ApiService} from "@services/api.service";
import {AuthenticationService} from "@services/authentication.service";

@Component({
    selector: 'app-confirm',
    templateUrl: './success.component.html',
    styleUrls: ['success.component.css']
})
export class SuccessComponent implements OnInit {
    message: string;
    type: string;

    constructor(private activatedRoute: ActivatedRoute,
                private translate: TranslateService,
                private apiService: ApiService,
                private authSrv: AuthenticationService) {
        if (!this.authSrv.currentAccount) {
            this.apiService.get(environment.default_lang_url).subscribe(defaults => {
                if (defaults) {
                    let lang = defaults.language;
                    this.translate.setDefaultLang(lang);
                    this.translate.use(lang)
                }
            })
        }
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
