import {Injectable} from '@angular/core';
import {Subject} from "rxjs";

@Injectable({
    providedIn: 'root'
})
export class ActionsService {

    constructor() {
    }

    private emitChangeSource = new Subject<any>();
    refreshEmitted = this.emitChangeSource.asObservable();

    // Service message commands
    emmitAction(action: Action) {
        this.emitChangeSource.next(action);
    }
}

export enum Action {
    REFRESH, EDIT, SHARE, CLEANUP, LEAVE, ARCHIVE, ADD
}


