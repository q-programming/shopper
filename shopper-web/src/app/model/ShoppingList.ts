import {ListItem} from "./ListItem";
import {CategoryPreset} from "./CategoryPreset";

export class ShoppingList {
    id: number;
    name: string;
    items: ListItem[];
    ownerId: string;
    ownerName: string;
    ownerAvatar: any;
    isOwner: boolean;
    done: number;
    archived: boolean;
    shared: string[];
    preset: CategoryPreset;

    constructor() {

    }
}

