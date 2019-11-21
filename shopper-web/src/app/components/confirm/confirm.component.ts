import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {ApiService} from "@services/api.service";
import {AlertService} from "@services/alert.service";
import {environment} from "@env/environment.prod";
import {AuthenticationService} from "@services/authentication.service";

@Component({
    selector: 'app-confirm',
    templateUrl: './confirm.component.html',
    styles: []
})
export class ConfirmComponent implements OnInit {


    token: string;

    constructor(private apiSrv: ApiService, private alertSrv: AlertService, private authSrv: AuthenticationService,
                private activatedRoute: ActivatedRoute,
                private router: Router,) {
    }

    ngOnInit() {
        this.authSrv.setLanguage();
        this.activatedRoute.params.subscribe(params => {
            this.token = params['token'];
            if (!this.token) {
                this.alertSrv.error('user.confirm.token.error.missing');
            }
            this.apiSrv.post(`${environment.auth_url}/confirm`, this.token).subscribe(response => {
                switch (response.result) {
                    case 'confirmed':
                        this.alertSrv.success('app.success.confirmed');
                        break;
                    case 'device_confirmed':
                        this.alertSrv.success('app.success.device.confirmed');
                        break;
                }
                this.router.navigate(['/']);
            }, error => {
                this.switchErrors(error)
            })
        });
    }

    private switchErrors(error: any) {
        if (error.status === 404) {
            this.alertSrv.error('app.error.token.invalid');
        } else if (error.status === 409) {
            switch (error.error) {
                case 'expired':
                    this.alertSrv.error('app.error.token.expired');
                    break;
            }
        }
        this.router.navigate(['/']);
    }

}
