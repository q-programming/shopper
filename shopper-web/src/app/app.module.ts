import {BrowserModule} from '@angular/platform-browser';
import {APP_INITIALIZER, NgModule, NO_ERRORS_SCHEMA} from '@angular/core';
import {HTTP_INTERCEPTORS, HttpClient, HttpClientModule} from "@angular/common/http";

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
import {LoggerModule, NgxLoggerLevel} from 'ngx-logger';
import {AvatarService} from "./services/avatar.service";
import {AccountService} from "./services/account.service";
import {AvatarUploadComponent, SettingsComponent} from './components/settings/settings.component';
import {ImageCropperModule} from "ngx-img-cropper";
import {TranslateLoader, TranslateModule} from "@ngx-translate/core";
import {TranslateHttpLoader} from "@ngx-translate/http-loader";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {AppMaterialModules} from "./material.module";
import {FlexLayoutModule} from "@angular/flex-layout";
import {LayoutModule} from '@angular/cdk/layout';
import {MatToolbarModule, MatButtonModule, MatSidenavModule, MatIconModule, MatListModule} from '@angular/material';
import {NgHttpLoaderModule} from "ng-http-loader";
import {LoaderComponent} from './components/loader/loader.component';
import {ListService} from "./services/list.service";
import {ListComponent} from './components/list/list.component';
import {FormBuilder, ReactiveFormsModule} from "@angular/forms";
import {NewShoppingListComponent} from "./components/dialogs/new-list/new-shoppinglist.component";
import {ShoppingListsComponent} from "./components/shoppinglists/shopping-lists.component";
import {FormsModule} from '@angular/forms';
import {ConfirmDialogComponent} from './components/dialogs/confirm/confirm-dialog.component';
import {ItemService} from "./services/item.service";


export function initUserFactory(authService: AuthenticationService) {
    return () => authService.initUser();
}


@NgModule({
    declarations: [
        AppComponent,
        LoginComponent,
        HomeComponent,
        AlertComponent,
        SettingsComponent,
        AvatarUploadComponent,
        LoaderComponent,
        ShoppingListsComponent,
        ListComponent,
        NewShoppingListComponent,
        ConfirmDialogComponent
    ],
    entryComponents: [
        AvatarUploadComponent,
        ShoppingListsComponent,
        NewShoppingListComponent,
        ConfirmDialogComponent,
        LoaderComponent
    ],
    imports: [
        FormsModule,
        ReactiveFormsModule,
        BrowserModule,
        BrowserAnimationsModule,
        HttpClientModule,
        routing,
        AppMaterialModules,
        NgHttpLoaderModule,
        FlexLayoutModule,
        LoggerModule.forRoot({
            level: NgxLoggerLevel.DEBUG,
            serverLogLevel: NgxLoggerLevel.ERROR
        }),
        ImageCropperModule,
        TranslateModule.forRoot({
            loader: {
                provide: TranslateLoader,
                useFactory: (createTranslateLoader),
                deps: [HttpClient]
            }
        }),
        LayoutModule,
        MatToolbarModule,
        MatButtonModule,
        MatSidenavModule,
        MatIconModule,
        MatListModule
    ],
    schemas: [NO_ERRORS_SCHEMA],
    providers: [
        AuthGuard,
        AuthenticationService,
        ApiService,
        AccountService,
        AvatarService,
        AlertService,
        ListService,
        ItemService,
        FormBuilder,
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
    exports: [ImageCropperModule],
    bootstrap: [AppComponent]
})
export class AppModule {
}

export function createTranslateLoader(http: HttpClient) {
    return new TranslateHttpLoader(http, './assets/i18n/', '.json');
}
