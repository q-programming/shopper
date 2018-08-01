export class Message {
    id: string;
    type: MessageType;
    text: string;

    constructor(text: string, type: MessageType) {
        this.id = Math.round((Math.random() * 36 ** 12)).toString(36);
        this.text = text;
        this.type = type;
    }
}


export enum MessageType {
    SUCCESS, ERROR, WARNING, INFO
}