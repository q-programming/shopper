import {AfterViewInit, Component, Inject, OnInit} from '@angular/core';
import {environment} from "@env/environment";
import {AuthenticationService, FACEBOOK_AUTH_URL, GOOGLE_AUTH_URL} from "@services/authentication.service";
import {ActivatedRoute, Router} from "@angular/router";
import {FormControl, Validators} from "@angular/forms";
import {DOCUMENT, ViewportScroller} from "@angular/common";
import {first} from "rxjs/operators";

@Component({
    selector: 'app-login',
    templateUrl: 'login.component.html',
    styleUrls: ['login.component.css']
})
export class LoginComponent implements OnInit, AfterViewInit {

    login_url = environment.context + environment.login_url;
    FacebookLoginURL;
    GoogleLoginURL;
    redirect_url;

    usernameCtrl = new FormControl('', Validators.required);
    passwordCtrl = new FormControl('', Validators.required);

    constructor(
        private route: ActivatedRoute,
        private viewportScroller: ViewportScroller,
        private authSrv: AuthenticationService,
        private router: Router,
        @Inject(DOCUMENT) private document: Document) {
        this.authSrv.setLanguage();
    }

    ngOnInit() {
        if (this.authSrv.currentAccount) {
            this.router.navigate(['/']);
        }
        this.redirect_url = `${this.document.location.href.split("#")[0]}#/`;
        this.FacebookLoginURL = FACEBOOK_AUTH_URL + this.redirect_url;
        this.GoogleLoginURL = GOOGLE_AUTH_URL + this.redirect_url;
    }

    ngAfterViewInit(): void {
        setTimeout(() => {
            this.route.fragment.pipe(first()).subscribe(fragment => {
                this.viewportScroller.scrollToAnchor(fragment);
            });
        }, 0)
    }


    login() {
        this.authSrv.login(this.usernameCtrl.value, this.passwordCtrl.value).subscribe((account) => {
            if (account) {
                this.router.navigate(['/']);
            }
        })
    }
}
