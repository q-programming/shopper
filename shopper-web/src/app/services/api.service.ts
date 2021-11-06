import {HttpClient, HttpHeaders, HttpRequest, HttpResponse} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {serialize} from "../utils/serialize";
import {environment} from "@env/environment";
import {AlertService} from "./alert.service";
import {NgProgress, NgProgressRef} from "ngx-progressbar";
import {catchError, filter, map} from 'rxjs/operators';
import {TranslateService} from "@ngx-translate/core";
import {Observable} from "rxjs";

export enum RequestMethod {
    Get = 'GET',
    Head = 'HEAD',
    Post = 'POST',
    Put = 'PUT',
    Delete = 'DELETE',
    Options = 'OPTIONS',
    Patch = 'PATCH'
}

@Injectable()
export class ApiService {

    progress: NgProgressRef;
    headers = new HttpHeaders({
        'Accept': 'application/json',
        'Content-Type': 'application/json'
    });

    constructor(private http: HttpClient, private alertSrv: AlertService, public ngProgress: NgProgress, private translate: TranslateService) {
        this.progress = ngProgress.ref();
    }

    get(path: string, args?: any): Observable<any> {
        path = environment.context + path;
        const options = {
            headers: this.headers,
            withCredentials: true
        };
        if (args) {
            options['params'] = serialize(args);
        }
        return this.http.get(path, options)
            .pipe(
                map((response) => {
                    this.progress.complete();
                    return response
                }), catchError(error => this.checkError(error)));
    }

    getObject<R>(path: string, args?: any): Observable<any> {
        path = environment.context + path;
        const options = {
            headers: this.headers,
            withCredentials: true
        };
        if (args) {
            options['params'] = serialize(args);
        }
        return this.http.get<R>(path, options).pipe(
            catchError(error => this.checkError(error))
        );
    }

    postObject<R>(path: string, body: any, customHeaders?: HttpHeaders): Observable<any> {
        return this.requestObject<R>(path, body, RequestMethod.Post, customHeaders);
    }

    login(path: string, body: any): Observable<any> {
        const xformHeader = new HttpHeaders().set('Content-Type', 'application/x-www-form-urlencoded');
        return this.request(path, body.toString(), RequestMethod.Post, xformHeader, false);
    }


    post(path: string, body: any, customHeaders?: HttpHeaders, showAlerts: boolean = true): Observable<any> {
        return this.request(path, body, RequestMethod.Post, customHeaders, showAlerts);
    }

    put(path: string, body: any): Observable<any> {
        return this.request(path, body, RequestMethod.Put);
    }

    delete(path: string, body?: any): Observable<any> {
        return this.request(path, body, RequestMethod.Delete);
    }

    private requestObject<R>(path: string, body: any, method = RequestMethod.Post, custemHeaders?: HttpHeaders): Observable<any> {
        path = environment.context + path;
        const req = new HttpRequest(method, path, body, {
            headers: custemHeaders || this.headers,
            withCredentials: true
        });

        return this.http.request<R>(req).pipe(
            map((response: HttpResponse<R>) => response.body),
            catchError(error => this.checkError(error)));
    }

    private request(path: string, body: any, method = RequestMethod.Post, customHeaders?: HttpHeaders, showAlerts: boolean = true): Observable<any> {
        this.progress.start();
        path = environment.context + path;
        const req = new HttpRequest(method, path, body, {
            headers: customHeaders || this.headers,
            withCredentials: true
        });

        return this.http.request(req)
            .pipe(
                filter(response => response instanceof HttpResponse),
                map((response: HttpResponse<any>) => {
                    this.progress.complete();
                    return response.body
                }),
                catchError(error => this.checkError(error, showAlerts)));
    }

    // Display error if logged in, otherwise redirect to IDP
    private checkError(error: any, showAlerts: boolean = true): any {
        this.progress.complete();
        if (showAlerts) {
            if (error && error.status === 401) {
                this.alertSrv.error('app.api.error.unauthorized');
                // this.redirectIfUnauth(error);
            } else if (error && error.status === 404) {
                this.alertSrv.error('app.api.error.notfound');
            } else if (error && error.status === 403) {
                this.alertSrv.error('app.api.error.unauthorized');
            } else if (error && error.status === 503) {
            }
        }
        throw error;
    }

}
