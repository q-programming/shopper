import { Component, OnInit } from '@angular/core';
import {AlertService} from "../../../services/alert.service";
import {ApiService} from "../../../services/api.service";

@Component({
  selector: 'settings-products',
  templateUrl: './products-settings.component.html',
  styles: []
})
export class ProductsSettingsComponent implements OnInit {

  constructor(private alertSrv: AlertService, private apiSrv: ApiService) { }

  ngOnInit() {
  }

}
