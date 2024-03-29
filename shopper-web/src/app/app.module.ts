import {BrowserModule} from '@angular/platform-browser';
import {APP_INITIALIZER, NgModule, NO_ERRORS_SCHEMA} from '@angular/core';
import {HTTP_INTERCEPTORS, HttpClient, HttpClientModule} from "@angular/common/http";
import {routing} from "./app.routing";
import {AppComponent} from './app.component';
import {LoginComponent} from './components/login/login.component';
import {AuthenticationService} from "@services/authentication.service";
import {AuthInterceptor} from "./guards/auth.interceptor";
import {AlertComponent} from './components/alert/alert.component';
import {AlertService} from "@services/alert.service";
import {AuthGuard} from "./guards/auth.guard";
import {ApiService} from "@services/api.service";
import {LoggerModule, NgxLoggerLevel} from 'ngx-logger';
import {AvatarService} from "@services/avatar.service";
import {AccountService} from "@services/account.service";
import {SettingsComponent} from './components/settings/settings.component';
import {ImageCropperModule} from "ngx-img-cropper";
import {TranslateLoader, TranslateModule} from "@ngx-translate/core";
import {TranslateHttpLoader} from "@ngx-translate/http-loader";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {AppMaterialModules} from "./material.module";
import {FlexLayoutModule} from "@angular/flex-layout";
import {LayoutModule} from '@angular/cdk/layout';
import {NgxMatSelectSearchModule} from 'ngx-mat-select-search';
import {ListService} from "@services/list.service";
import {ListComponent} from './components/list/list.component';
import {FormBuilder, FormsModule, ReactiveFormsModule} from "@angular/forms";
import {ShoppingListDialogComponent} from "./components/dialogs/list/shopping-list-dialog.component";
import {ShoppingListsComponent} from "./components/shoppinglists/shopping-lists.component";
import {ConfirmDialogComponent} from './components/dialogs/confirm/confirm-dialog.component';
import {ItemService} from "@services/item.service";
import {ItemDialogComponent} from "./components/dialogs/item/item-dialog.component";
import {HighlightDirective} from './directives/highlight.directive';
import {QuickaddComponent} from './components/list/quickadd/quickadd.component';
import {ShareComponent} from './components/dialogs/share/share.component';
import {EmailSettingsComponent} from './components/settings/email/email-settings.component';
import {InfiniteScrollModule} from 'ngx-infinite-scroll';
import {
    AccountSettingsComponent,
    AvatarUploadComponent
} from './components/settings/account/account-settings.component';
import {CategorySortingSettingsComponent} from './components/settings/category/category-sorting-settings.component';
import {NotificationsSettingsComponent} from './components/settings/notifications/notifications-settings.component';
import {AppSettingsComponent} from "./components/settings/app/app-settings.component";
import {SharedWithComponent} from './components/dialogs/share/shared-with/shared-with.component';
import {NewShareComponent} from './components/dialogs/share/new-share/new-share.component';
import {HelpComponent} from './components/help/help.component';
import {MenuToolbarComponent} from './components/menu/toolbar/menu-toolbar.component';
import {MenuSideNavComponent} from './components/menu/sidenav/menu-side-nav.component';
import {FavoritesComponent} from './components/favorites/favorites.component';
import {ItemDetailsComponent} from './components/dialogs/item/item-details/item-details.component';
import {TrimDirective} from './directives/trim.directive';
import {environment} from "@env/environment";
import {PullToRefreshComponent} from './components/pull-to-refresh/pull-to-refresh.component';
import {EnHelpComponent} from './components/help/en/en-help.component';
import {PlHelpComponent} from './components/help/pl/pl-help.component';
import {RegisterComponent} from './components/login/register/register.component';
import {ErrorComponent} from './components/error/error.component';
import {SuccessComponent} from "./components/success/success.component";
import {DragDropModule} from "@angular/cdk/drag-drop";
import {NgProgressModule} from 'ngx-progressbar';
import {ResetPasswordComponent} from './components/login/reset-password/reset-password.component';
import {ChangePasswordComponent} from "./components/login/change-password/change-password.component";
import {ConfirmComponent} from "./components/confirm/confirm.component";
import {BackdropComponent} from './components/menu/backdrop/backdrop.component';
import {ItemComponent} from './components/list/item/item.component';
import {InnerLoaderComponent} from './components/inner-loader/inner-loader.component';
import {DevicesComponent} from './components/settings/devices/devices/devices.component';
import {ScrollTopComponent} from "./components/scroll-top/scroll-top.component";

export function initUserFactory(authService: AuthenticationService) {
    return () => authService.initUser();
}


@NgModule({
    declarations: [
        AppComponent,
        LoginComponent,
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
        CategorySortingSettingsComponent,
        NotificationsSettingsComponent,
        AppSettingsComponent,
        SharedWithComponent,
        NewShareComponent,
        HelpComponent,
        MenuToolbarComponent,
        MenuSideNavComponent,
        FavoritesComponent,
        ItemDetailsComponent,
        TrimDirective,
        PullToRefreshComponent,
        EnHelpComponent,
        PlHelpComponent,
        RegisterComponent,
        SuccessComponent,
        ErrorComponent,
        ResetPasswordComponent,
        ChangePasswordComponent,
        ConfirmComponent,
        BackdropComponent,
        ItemComponent,
        InnerLoaderComponent,
        DevicesComponent,
        ScrollTopComponent
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
        DragDropModule,
        InfiniteScrollModule,
        LoggerModule.forRoot({
            level: environment.logging,
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