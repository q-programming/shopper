import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Router} from "@angular/router";

@Injectable()
export class AuthenticationService {

    constructor(private http: HttpClient, private router: Router) {
    }

    logout() {
        this.http.post('logout', {}).finally(() => {
            localStorage.removeItem('currentUser');
            this.router.navigateByUrl('/login');
        }).subscribe();
    }
}
