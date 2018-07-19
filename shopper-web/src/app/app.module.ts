import {BrowserModule} from '@angular/platform-browser';
import {APP_INITIALIZER, NgModule, NO_ERRORS_SCHEMA} from '@angular/core';
import {HTTP_INTERCEPTORS, HttpClientModule} from "@angular/common/http";
import {MDBBootstrapModule} from 'angular-bootstrap-md';
import {NgHttpLoaderModule} from "ng-http-loader/ng-http-loader.module";

import {routing} from "./app.routing";
import {AppComponent} from './app.component';
import {LoginComponent} from './components/login/login.component';
import {HomeComponent} from './components/home/home.component';
import {AuthenticationService} from "./services/authentication.service";
import {AuthInterceptor} from "./guards/auth.interceptor";
import {AlertComponent} from './directives/alert.component';
import {AlertService} from "./services/alert.service";
import {AuthGuard} from "./guards/auth.guard";
import {ApiService} from "./services/api.service";
import { HeaderComponent } from './components/header/header.component';
import { AccountMenuComponent } from './components/header/account-menu/account-menu.component';

export function initUserFactory(authService: AuthenticationService) {
    return () => authService.initUser();
}


@NgModule({
    declarations: [
        AppComponent,
        LoginComponent,
        HomeComponent,
        AlertComponent,
        HeaderComponent,
        AccountMenuComponent
    ],
    imports: [
        BrowserModule,
        MDBBootstrapModule.forRoot(),
        NgHttpLoaderModule,
        HttpClientModule,
        routing

    ],
    schemas: [NO_ERRORS_SCHEMA],
    providers: [
        AuthGuard,
        AuthenticationService,
        ApiService,
        AlertService,
        {
            provide: HTTP_INTERCEPTORS,
            useClass: AuthInterceptor,
            multi: true
        },
        {
            'provide': APP_INITIALIZER,
            'useFactory': initUserFactory,
            'deps': [AuthenticationService],
            'multi': true
        }
    ],
    bootstrap: [AppComponent]
})
export class AppModule {
}
