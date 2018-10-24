import {Product} from "./Product";
import {Category} from "./Category";

export class ListItem {
    id?: number;
    product: Product;
    name?: string;
    description?: string;
    quantity?: number;
    unit?: string;
    category?: Category;
    done?: boolean;

    displayName() {
        return this.name ? this.name : this.product.name;
    }
}

