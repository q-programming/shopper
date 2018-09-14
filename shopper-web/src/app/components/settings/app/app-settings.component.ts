import {Component, OnInit} from '@angular/core';
import {AlertService} from "../../../services/alert.service";
import {ApiService} from "../../../services/api.service";

@Component({
    selector: 'settings-app',
    templateUrl: './app-settings.component.html',
    styles: []
})
export class AppSettingsComponent implements OnInit {

    constructor(private alertSrv: AlertService, private apiSrv: ApiService) {
    }

    ngOnInit() {
    }

    testMessages() {
        this.alertSrv.successMessage("Success");
        this.alertSrv.errorMessage("Error");
        this.alertSrv.warningMessage("Warning");
        this.alertSrv.infoMessage("Info");
    }


}
