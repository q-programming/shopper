import {Component, Inject, OnInit, ViewChild} from '@angular/core';
import {Account} from "@model/Account";
import {languages} from "../../../../assets/i18n/languages";
import {MAT_DIALOG_DATA, MatDialog} from "@angular/material";
import {AuthenticationService} from "@services/authentication.service";
import {ApiService} from "@services/api.service";
import {NGXLogger} from "ngx-logger";
import {AlertService} from "@services/alert.service";
import {AvatarService} from "@services/avatar.service";
import {TranslateService} from "@ngx-translate/core";
import {CropperSettings, ImageCropperComponent} from "ngx-img-cropper";
import {environment} from "@env/environment";
import {getBase64Image} from "../../../utils/utils";
import {ItemService} from "@services/item.service";

@Component({
    selector: 'settings-account',
    templateUrl: './account-settings.component.html',
    styles: []
})
export class AccountSettingsComponent implements OnInit {

    account: Account;
    avatarData: any = {};
    languages: any = languages;

    constructor(public dialog: MatDialog,
                private authSrv: AuthenticationService,
                private api: ApiService,
                private logger: NGXLogger,
                private alertSrv: AlertService,
                private avatarSrv: AvatarService,
                private itemSrv: ItemService,
                private translate: TranslateService) {
    }

    ngOnInit() {
        this.account = this.authSrv.currentAccount;
        this.avatarData.image = this.account.avatar;
    }


    changeLanguage() {
        this.api.post(`${environment.account_url}${environment.language_url}`, this.account.language).subscribe(() => {
            this.translate.use(this.account.language).subscribe(() => {
                this.alertSrv.success('app.settings.account.language.success');
                this.itemSrv.loadCategoriesWithLocalName();
            });
        })
    }

    uploadNewAvatar() {
        this.avatarSrv.updateAvatar(getBase64Image(this.avatarData.image), this.account);
        // this.avatarUploadModal.hide();
        this.alertSrv.success('app.settings.account.avatar.success');
    }


    openDialog() {
        const dialogRef = this.dialog.open(AvatarUploadComponent, {
            panelClass: 'shopper-modal-normal',
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
    selector: 'settings-avatar-upload',
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
        this.cropperSettings.canvasWidth = 350;
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
