export class AppSettings {
    email: EmailSettings;

    constructor() {
        this.email = new EmailSettings();
    }

}

export class EmailSettings {
    url: string = '';
    port: number = 25;
    username: string = '';
    password: string = '';
    encoding: string = '';
    from: string = '';
}

