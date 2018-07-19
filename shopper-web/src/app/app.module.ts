import {BrowserModule} from '@angular/platform-browser';
import {NgModule, NO_ERRORS_SCHEMA} from '@angular/core';
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


@NgModule({
    declarations: [
        AppComponent,
        LoginComponent,
        HomeComponent,
        AlertComponent
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
        AlertService,
        {
            provide: HTTP_INTERCEPTORS,
            useClass: AuthInterceptor,
            multi: true
        }
    ],
    bootstrap: [AppComponent]
})
export class AppModule {
}
