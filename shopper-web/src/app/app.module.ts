import {BrowserModule} from '@angular/platform-browser';
import {APP_INITIALIZER, NgModule, NO_ERRORS_SCHEMA} from '@angular/core';
import {HTTP_INTERCEPTORS, HttpClient, HttpClientModule} from "@angular/common/http";

import {routing} from "./app.routing";
import {AppComponent} from './app.component';
import {LoginComponent} from './components/login/login.component';
import {HomeComponent} from './components/home/home.component';
import {AuthenticationService} from "./services/authentication.service";
import {AuthInterceptor} from "./guards/auth.interceptor";
import {AlertComponent} from './components/alert/alert.component';
import {AlertService} from "./services/alert.service";
import {AuthGuard} from "./guards/auth.guard";
import {ApiService} from "./services/api.service";
import {LoggerModule, NgxLoggerLevel} from 'ngx-logger';
import {AvatarService} from "./services/avatar.service";
import {AccountService} from "./services/account.service";
import {SettingsComponent} from './components/settings/settings.component';
import {ImageCropperModule} from "ngx-img-cropper";
import {TranslateLoader, TranslateModule} from "@ngx-translate/core";
import {TranslateHttpLoader} from "@ngx-translate/http-loader";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {AppMaterialModules} from "./material.module";
import {FlexLayoutModule} from "@angular/flex-layout";
import {LayoutModule} from '@angular/cdk/layout';
import {MatButtonModule, MatIconModule, MatListModule, MatSidenavModule, MatToolbarModule} from '@angular/material';
import {NgxMatSelectSearchModule} from 'ngx-mat-select-search';
import {ListService} from "./services/list.service";
import {ListComponent} from './components/list/list.component';
import {FormBuilder, FormsModule, ReactiveFormsModule} from "@angular/forms";
import {ShoppingListDialogComponent} from "./components/dialogs/list/shopping-list-dialog.component";
import {ShoppingListsComponent} from "./components/shoppinglists/shopping-lists.component";
import {ConfirmDialogComponent} from './components/dialogs/confirm/confirm-dialog.component';
import {ItemService} from "./services/item.service";
import {ItemDialogComponent} from "./components/dialogs/item/item-dialog.component";
import {HighlightDirective} from './directives/highlight.directive';
import {QuickaddComponent} from './components/list/quickadd/quickadd.component';
import {ShareComponent} from './components/dialogs/share/share.component';
import {EmailSettingsComponent} from './components/settings/email/email-settings.component';
import {
    AccountSettingsComponent,
    AvatarUploadComponent
} from './components/settings/account/account-settings.component';
import {ProductsSettingsComponent} from './components/settings/products/products-settings.component';
import {NotificationsSettingsComponent} from './components/settings/notifications/notifications-settings.component';
import {AppSettingsComponent} from "./components/settings/app/app-settings.component";
import {CookieService} from "ngx-cookie-service";
import {SharedWithComponent} from './components/dialogs/share/shared-with/shared-with.component';
import {NewShareComponent} from './components/dialogs/share/new-share/new-share.component';
import {DndModule} from "ng2-dnd";
import {NgProgressModule} from "ngx-progressbar";
import {HelpComponent} from './components/help/help.component';

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
        ShoppingListsComponent,
        ListComponent,
        ShoppingListDialogComponent,
        ConfirmDialogComponent,
        ItemDialogComponent,
        HighlightDirective,
        QuickaddComponent,
        ShareComponent,
        EmailSettingsComponent,
        AccountSettingsComponent,
        ProductsSettingsComponent,
        NotificationsSettingsComponent,
        AppSettingsComponent,
        SharedWithComponent,
        NewShareComponent,
        HelpComponent
    ],
    entryComponents: [
        AvatarUploadComponent,
        ShoppingListsComponent,
        ShoppingListDialogComponent,
        ConfirmDialogComponent,
        ItemDialogComponent,
        ShareComponent
    ],
    imports: [
        FormsModule,
        ReactiveFormsModule,
        BrowserModule,
        BrowserAnimationsModule,
        HttpClientModule,
        routing,
        AppMaterialModules,
        NgProgressModule,
        FlexLayoutModule,
        DndModule.forRoot(),
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
        NgxMatSelectSearchModule,
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
        CookieService,
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
