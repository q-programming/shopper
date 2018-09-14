import {Component, OnInit} from '@angular/core';
import {AlertService} from "../../../services/alert.service";
import {ApiService} from "../../../services/api.service";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {AppSettings} from "../../../model/AppSettings";

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
            url: [this.settings.email.url, Validators.required],
            port: [this.settings.email.port, Validators.required],
            username: [this.settings.email.username, Validators.required],
            password: [this.settings.email.password, Validators.required],
            encoding: [this.settings.email.encoding, Validators.required],
            from: [this.settings.email.from, Validators.required, Validators.email],
        })
    }

    ngOnInit() {
    }

}
