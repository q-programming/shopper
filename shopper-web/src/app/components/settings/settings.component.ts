import {Component, OnInit, ViewChild} from '@angular/core';
import {AuthenticationService} from "../../services/authentication.service";
import {Account} from "../../model/Account";
import {ApiService} from "../../services/api.service";
import {environment} from "../../../environments/environment";
import {NGXLogger} from "ngx-logger";
import {AlertService} from "../../services/alert.service";
import {ModalDirective} from "angular-bootstrap-md";
import {getBase64Image} from "../../utils/utils";
import {CropperSettings, ImageCropperComponent} from "ngx-img-cropper";
import {AvatarService} from "../../services/avatar.service";


@Component({
    selector: 'app-settings',
    templateUrl: './settings.component.html',
    styles: []
})
export class SettingsComponent implements OnInit {

    account: Account;
    cropperSettings: CropperSettings;
    avatarData: any = {};
    @ViewChild('cropper', undefined)
    cropper: ImageCropperComponent;
    @ViewChild('avatarUploadModal')
    avatarUploadModal: ModalDirective;

    constructor(private authSrv: AuthenticationService, private api: ApiService, private logger: NGXLogger, private alertSrv: AlertService, private avatarSrv: AvatarService) {
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


    ngOnInit() {
        this.account = this.authSrv.currentAccount;
        this.avatarData.image = this.account.avatar;
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

    testPost() {
        this.api.post(`${environment.account_url}/test`, this.account).subscribe(() => this.logger.info("works"))
    }

    uploadNewAvatar() {
        this.avatarSrv.updateAvatar(getBase64Image(this.avatarData.image), this.account);
        this.avatarUploadModal.hide();
    }
}
