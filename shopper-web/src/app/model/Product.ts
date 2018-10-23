import {Category} from "./Category";

export class Product {
    id?: number;
    name: string;
    topCategory?: Category;

    constructor(name:string){
        this.name = name;
    }
}
