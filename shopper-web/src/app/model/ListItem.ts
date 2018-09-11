import {Product} from "./Product";
import {Category} from "./Category";

export class ListItem {
    id?: number;
    product: Product;
    description?: string;
    quantity?: number;
    unit?: string;
    category?: Category;
    done?: boolean;
}

