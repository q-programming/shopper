import {Category} from "./Category";

export class Product {
    id?: number;
    name: String;
    topCategory?: Category;

    constructor(name:String){
        this.name = name;
    }
}
