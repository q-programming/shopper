import {RouterModule, Routes} from '@angular/router';
import {LoginComponent} from "./components/login/login.component";
import {AuthGuard} from "./guards/auth.guard";
import {SettingsComponent} from "./components/settings/settings.component";
import {ShoppingListsComponent} from "./components/shoppinglists/shopping-lists.component";
import {HomeComponent} from "./components/home/home.component";
import {ListComponent} from "./components/list/list.component";
import {HelpComponent} from "./components/help/help.component";
import {RegisterComponent} from "./components/register/register.component";

const appRoutes: Routes = [
    {path: '', component: ShoppingListsComponent, canActivate: [AuthGuard]},
    {path: 'settings', component: SettingsComponent, canActivate: [AuthGuard]},
    {path: 'login', component: LoginComponent},
    {path: ':userid/lists', component: ShoppingListsComponent, canActivate: [AuthGuard]},
    {path: 'list/:listid', component: ListComponent, canActivate: [AuthGuard]},
    {path: 'home', component: HomeComponent, canActivate: [AuthGuard]},
    {path: 'help', component: HelpComponent, canActivate: [AuthGuard]},
    {path: 'register', component: RegisterComponent},
    // otherwise redirect to home
    {path: '**', redirectTo: ''}
];

export const routing = RouterModule.forRoot(appRoutes, {
    anchorScrolling: 'enabled',
    useHash: true,
    onSameUrlNavigation: 'reload'
});
