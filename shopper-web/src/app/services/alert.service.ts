import {Injectable} from '@angular/core';
import {Message, MessageType} from "../model/Message";
import {Observable} from "rxjs/Observable";
import {TranslateService} from "@ngx-translate/core";
import * as _ from 'lodash';
import {Subscriber} from "rxjs";

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


    success(key: string, params?: Object, timeout: number = this.normal_timeout) {
        this.translate.get(key, params).subscribe(txt => {
            this.addMessage(txt, MessageType.SUCCESS, timeout);
        })
    }


    error(key: string, params?: Object, timeout: number = this.error_timeout) {
        this.translate.get(key, params).subscribe(txt => {
            this.addMessage(txt, MessageType.ERROR, timeout);
        })
    }

    warning(key: string, params?: Object, timeout: number = this.normal_timeout) {
        this.translate.get(key, params).subscribe(txt => {
            this.addMessage(txt, MessageType.WARNING, timeout);
        })

    }

    info(key: string, params?: Object, timeout: number = this.error_timeout) {
        this.translate.get(key, params).subscribe(txt => {
            this.addMessage(txt, MessageType.INFO, timeout);
        })
    }

    undoable(key: string, params?: Object): Observable<boolean> {
        return new Observable((observable) => {
            this.translate.get(key, params).subscribe(txt => {
                this.addMessage(txt, MessageType.UNDOABLE, 5000, observable);
            })
        });
    }

    private addMessage(text: string, type: MessageType, timeout: number, observable?: Subscriber<boolean>) {
        let message = new Message(text, type, observable);
        if (!this.exists(message)) {
            this.messages.push(message);
            Observable.timer(timeout).subscribe(() => {
                this.dissmis(message)
            });
        }
    }

    dissmis(message: Message) {
        _.remove(this.messages, (m) => {
            return m.id === message.id
        });
        if (message.type == MessageType.UNDOABLE && message.undoObservable) {
            if (message.undo) {
                message.undoObservable.next(true);
            } else {
                message.undoObservable.next(false);
            }
            message.undoObservable.complete();
        }
    }

    exists(message: Message) {
        return _.find(this.messages, (m) => {
            return m.type === message.type && m.text === message.text
        })
    }

    undo(message: Message) {
        message.undo = true;
        this.dissmis(message)
    }
}
