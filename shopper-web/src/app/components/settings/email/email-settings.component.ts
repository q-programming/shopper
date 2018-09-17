import {Component, OnInit} from '@angular/core';
import {AlertService} from "../../../services/alert.service";
import {ApiService} from "../../../services/api.service";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {AppSettings} from "../../../model/AppSettings";
import {environment} from "../../../../environments/environment";

@Component({
    selector: 'settings-email',
    templateUrl: './email-settings.component.html',
    styles: []
})
export class EmailSettingsComponent implements OnInit {

    form: FormGroup;
    settings: AppSettings = new AppSettings();
    encodings = ["UTF-8"];

    constructor(private alertSrv: AlertService, private apiSrv: ApiService, private formBuilder: FormBuilder) {
        this.form = this.formBuilder.group({
            host: [this.settings.email.host, Validators.required],
            port: [this.settings.email.port, Validators.required],
            username: [this.settings.email.username, Validators.required],
            password: [this.settings.email.password, Validators.required],
            encoding: [this.settings.email.encoding, Validators.required],
            from: [this.settings.email.from, [Validators.required, Validators.email]],
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
            this.apiSrv.post(`${environment.config_url}/settings/email`, this.settings).subscribe(() => {
                this.alertSrv.success('app.settings.email.save.success');
            }, error => {
                this.alertSrv.warning('app.settings.email.save.fail');
            })
        }
    }

    private getSettings() {
        Object.keys(this.form.controls).forEach(name => {
            this.settings.email[name] = this.form.controls[name].value
        });
    }

    private setSettings() {
        Object.keys(this.settings.email).forEach(name => {
            this.form.controls[name].setValue(this.settings.email[name]);
        });
    }
}
