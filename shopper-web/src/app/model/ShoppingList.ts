import {ListItem} from "./ListItem";

export class ShoppingList {
    id: number;
    name: string;
    items: ListItem[];
    ownerId: string;
    ownerName: string;
    ownerAvatar: any;
    notOwner:boolean;
    done:number;
    archived: boolean;
    shared: string[];
}

