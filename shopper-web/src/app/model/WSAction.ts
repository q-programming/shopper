export class WSAction {
    action: WSActionType;
    user?: string;
    listID: number;
    message?: string;
}

export enum WSActionType {
    ADD = "ADD", REMOVE = "REMOVE", DONE = "DONE", REFRESH = "REFRESH"
}