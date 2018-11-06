import {Injectable} from '@angular/core';
import {Subject} from "rxjs";

@Injectable({
    providedIn: 'root'
})
export class MenuActionsService {

    private emitActionSource = new Subject<any>();
    actionEmitted = this.emitActionSource.asObservable();


    constructor() {
    }


    /**
     * Emmits some action performed. All components which are subscribing to this service can be notified and perform some actions based on it
     * @param action action to be handled in components
     */
    emmitAction(action: MenuAction) {
        this.emitActionSource.next(action);
    }
}

export enum MenuAction {
    REFRESH, EDIT, SHARE, CLEANUP, LEAVE, ARCHIVE, ADD, ADD_ITEM, PENDING_REFRESH
}
