import {Injectable} from '@angular/core';
import {Message, MessageType} from "../model/Message";
import {Observable} from "rxjs/Observable";
import {TranslateService} from "@ngx-translate/core";
import * as _ from 'lodash';

@Injectable()
export class AlertService {

    private messages: Message[] = [];
    private normal_timeout: number = 4000; //4 seconds
    private error_timeout: number = 6000; //6 seconds

    constructor(private translate: TranslateService) {
        this.translate.setDefaultLang('en');
    }


    getMessages(): Message[] {
        return this.messages;
    }

    successMessage(message: string, timeout: number = this.normal_timeout) {
        this.addMessage(message, MessageType.SUCCESS, timeout);
    }

    errorMessage(message: string, timeout: number = this.error_timeout) {
        this.addMessage(message, MessageType.ERROR, timeout);
    }

    warningMessage(message: string, timeout: number = this.normal_timeout) {
        this.addMessage(message, MessageType.WARNING, timeout);
    }

    infoMessage(message: string, timeout: number = this.normal_timeout) {
        this.addMessage(message, MessageType.INFO, timeout);
    }


    success(key: string, timeout: number = this.normal_timeout) {
        this.translate.get(key).subscribe(txt => {
            this.addMessage(txt, MessageType.SUCCESS, timeout);
        })
    }


    error(key: string, timeout: number = this.error_timeout) {
        this.translate.get(key).subscribe(txt => {
            this.addMessage(txt, MessageType.ERROR, timeout);
        })
    }

    warning(key: string, timeout: number = this.normal_timeout) {
        this.translate.get(key).subscribe(txt => {
            this.addMessage(txt, MessageType.ERROR, timeout);
        })

    }

    info(key: string, timeout: number = this.error_timeout) {
        this.translate.get(key).subscribe(txt => {
            this.addMessage(txt, MessageType.ERROR, timeout);
        })
    }

    private addMessage(text: string, type: MessageType, timeout: number) {
        let message = new Message(text, type);
        this.messages.push(message);
        Observable.timer(timeout).subscribe(() => {
            this.dissmis(message)
        });
    }

    dissmis(message: Message) {
        _.remove(this.messages, (m) => {
            return m.id === message.id
        });
    }
}
