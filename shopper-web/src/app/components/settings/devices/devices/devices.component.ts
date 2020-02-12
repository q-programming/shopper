import {Component, OnInit} from '@angular/core';
import {AlertService} from "@services/alert.service";
import {ApiService} from "@services/api.service";
import {environment} from "@env/environment";
import {Device} from "@model/Device";
import {ConfirmDialog, ConfirmDialogComponent} from "../../../dialogs/confirm/confirm-dialog.component";
import {MatDialog, MatDialogConfig} from "@angular/material/dialog";

@Component({
    selector: 'settings-devices',
    templateUrl: './devices.component.html',
    styleUrls: ['./devices.component.css']
})
export class DevicesComponent implements OnInit {
    devices: Device[];
    loading: boolean;

    constructor(private alertSrv: AlertService, private api: ApiService, public dialog: MatDialog) {
    }

    ngOnInit() {
        this.loading = true;
        this.loadAllDevices();
    }

    private loadAllDevices() {
        this.api.getObject<Device[]>(`${environment.account_url}${environment.devices_url}`).subscribe((result) => {
            this.devices = result;
            this.loading = false;
        })
    }

    deleteDevice(id: string) {
        const data: ConfirmDialog = {
            title_key: 'app.settings.devices.remove.text',
            message_key: 'app.settings.devices.remove.confirm',
            action_key: 'app.general.delete',
            action_class: 'warn'
        };
        const dialogConfig: MatDialogConfig = {
            disableClose: true,
            panelClass: 'shopper-modal',
            data: data
        };
        let dialogRef = this.dialog.open(ConfirmDialogComponent, dialogConfig);
        dialogRef.afterClosed().subscribe(result => {
            if (result) {
                this.api.delete(`${environment.account_url}${environment.devices_url}/${id}/remove`).subscribe(() => {
                    this.alertSrv.success('app.settings.devices.remove.success');
                    this.loadAllDevices();
                });
            }
        })
    }

}
