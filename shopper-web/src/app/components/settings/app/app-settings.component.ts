import {Component, Inject, OnInit} from '@angular/core';
import {AlertService} from "@services/alert.service";
import {ApiService} from "@services/api.service";
import {languages} from "../../../../assets/i18n/languages";
import {AppSettings} from "@model/AppSettings";
import {environment} from "@env/environment";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {DOCUMENT} from "@angular/common";

@Component({
    selector: 'settings-app',
    templateUrl: './app-settings.component.html',
    styles: []
})
export class AppSettingsComponent implements OnInit {

    form: FormGroup;
    languages: any = languages;
    settings: AppSettings = new AppSettings();

    constructor(private alertSrv: AlertService, private apiSrv: ApiService, private formBuilder: FormBuilder, @Inject(DOCUMENT) private document: Document) {
        this.form = this.formBuilder.group({
            language: [this.settings.language, Validators.required],
            appUrl: [this.settings.appUrl, Validators.required]
        })
    }

    ngOnInit() {
        this.apiSrv.getObject<AppSettings>(`${environment.config_url}/settings`).subscribe(result => {
            this.settings = result;
            this.setSettings();
        })
    }

    saveConfiguration() {
        if (this.form.valid) {
            this.getSettings();
            this.apiSrv.post(`${environment.config_url}/settings/app`, this.settings).subscribe(() => {
                this.alertSrv.success('app.settings.app.save.success');
            }, error => {
                this.alertSrv.error('app.settings.app.save.fail');
            })
        }
    }


    getAppURL() {
        this.form.controls.appUrl.setValue(this.document.location.href.split("#")[0]);
    }

    testMessages() {
        this.alertSrv.successMessage("Success");
        this.alertSrv.errorMessage("Error");
        this.alertSrv.warningMessage("Warning");
        this.alertSrv.infoMessage("Info");
    }

    private getSettings() {
        this.settings.language = this.form.controls.language.value;
        this.settings.appUrl = this.form.controls.appUrl.value;
    }

    private setSettings() {
        this.form.controls.language.setValue(this.settings.language);
        this.form.controls.appUrl.setValue(this.settings.appUrl);
    }


}
