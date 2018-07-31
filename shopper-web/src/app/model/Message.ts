export class Message {
    type: MessageType;
    text: string;

    constructor(text: string, type: MessageType) {
        this.text = text;
        this.type = type;
    }
}


export enum MessageType {
    SUCCESS, ERROR, WARNING, INFO
}