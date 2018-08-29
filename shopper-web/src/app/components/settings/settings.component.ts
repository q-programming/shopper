import {Component, Inject, OnInit, ViewChild} from '@angular/core';
import {AuthenticationService} from "../../services/authentication.service";
import {Account} from "../../model/Account";
import {ApiService} from "../../services/api.service";
import {environment} from "../../../environments/environment";
import {languages} from "../../../assets/i18n/languages";
import {NGXLogger} from "ngx-logger";
import {AlertService} from "../../services/alert.service";
import {getBase64Image} from "../../utils/utils";
import {CropperSettings, ImageCropperComponent} from "ngx-img-cropper";
import {AvatarService} from "../../services/avatar.service";
import {TranslateService} from "@ngx-translate/core";
import {MAT_DIALOG_DATA, MatDialog} from "@angular/material";


@Component({
    selector: 'app-settings',
    templateUrl: './settings.component.html',
    styles: []
})
export class SettingsComponent implements OnInit {

    account: Account;
    avatarData: any = {};
    languages: any = languages;

    constructor(public dialog: MatDialog,
                private authSrv: AuthenticationService,
                private api: ApiService,
                private logger: NGXLogger,
                private alertSrv: AlertService,
                private avatarSrv: AvatarService,
                private translate: TranslateService) {
    }

    ngOnInit() {
        this.account = this.authSrv.currentAccount;
        this.avatarData.image = this.account.avatar;
    }


    testMessages() {
        this.alertSrv.successMessage("Success");
        this.alertSrv.errorMessage("Error");
        this.alertSrv.warningMessage("Warning");
        this.alertSrv.infoMessage("Info");
    }


    changeLanguage() {
        this.api.post(`${environment.account_url}${environment.language_url}`, this.account.language).subscribe(() => {
            this.translate.use(this.account.language).subscribe(() => {
                this.alertSrv.success('app.settings.language.success');
            });
        })
    }

    uploadNewAvatar() {
        this.avatarSrv.updateAvatar(getBase64Image(this.avatarData.image), this.account);
        // this.avatarUploadModal.hide();
        this.alertSrv.success('app.settings.avatar.success');
    }


    openDialog() {
        const dialogRef = this.dialog.open(AvatarUploadComponent, {
            data: {
                account: this.account,
                avatarData: this.avatarData
            }
        });
        dialogRef.afterClosed().subscribe((upload) => {
            if (upload) {
                this.uploadNewAvatar();
            }
        });
    }

}


@Component({
    selector: 'app-avatar-upload',
    templateUrl: './avatar-upload.component.html',
    styles: []
})
export class AvatarUploadComponent implements OnInit {

    @ViewChild('cropper', undefined)
    cropper: ImageCropperComponent;
    cropperSettings: CropperSettings;
    account: Account;
    avatarData: any;


    constructor(@Inject(MAT_DIALOG_DATA) public data: any) {
        this.account = data.account;
        this.avatarData = data.avatarData;
        this.cropperSettings = new CropperSettings();
        this.cropperSettings.width = 100;
        this.cropperSettings.height = 100;
        this.cropperSettings.croppedWidth = 100;
        this.cropperSettings.croppedHeight = 100;
        this.cropperSettings.canvasWidth = 400;
        this.cropperSettings.canvasHeight = 300;
        this.cropperSettings.noFileInput = true;
        this.cropperSettings.rounded = true;
    }

    ngOnInit(): void {
    }

    fileChangeListener($event) {
        let image: any = new Image();
        let file: File = $event.target.files[0];
        const myReader: FileReader = new FileReader();
        const that = this;
        myReader.onloadend = function (loadEvent: any) {
            image.src = loadEvent.target.result;
            that.cropper.setImage(image);
        };
        myReader.readAsDataURL(file);
    }
}

