export class User {
    name: String;
    surname: String;
    email: String;
    language: String;
    role: Role;
    authorities: Authority[]
}

export class Authority {
    authority: Role
}

export enum Role {
    ROLE_ADMIN = "ROLE_ADMIN", ROLE_USER = "ROLE_USER"
}