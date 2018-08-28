import {RouterModule, Routes} from '@angular/router';
import {LoginComponent} from "./components/login/login.component";
import {AuthGuard} from "./guards/auth.guard";
import {SettingsComponent} from "./components/settings/settings.component";
import {ShoppingListsComponent} from "./components/shoppinglists/shopping-lists.component";
import {HomeComponent} from "./components/home/home.component";

const appRoutes: Routes = [
    {path: '', component: ShoppingListsComponent, canActivate: [AuthGuard]},
    {path: 'settings', component: SettingsComponent, canActivate: [AuthGuard]},
    {path: 'login', component: LoginComponent},
    {path: 'list/:userid/', component: ShoppingListsComponent, canActivate: [AuthGuard]},
    {path: 'home', component: HomeComponent, canActivate: [AuthGuard]},

    // otherwise redirect to home
    {path: '**', redirectTo: ''}
];

export const routing = RouterModule.forRoot(appRoutes, {useHash: true});
