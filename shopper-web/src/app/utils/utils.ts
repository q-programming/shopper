import {ListItem} from "@model/ListItem";

export function getBase64Image(data: String): String {
    return data.replace(/^data:image\/(png|jpg|jpeg);base64,/, "");
}

export function itemDisplayName(item: ListItem): string {
    return item.name ? item.name : item.product.name;
}

export function Debounce(delay: number = 300): MethodDecorator {
    return function (target: any, propertyKey: string, descriptor: PropertyDescriptor) {
        let timeout = null;
        const original = descriptor.value;
        descriptor.value = function (...args) {
            clearTimeout(timeout);
            timeout = setTimeout(() => original.apply(this, args), delay);
        };
        return descriptor;
    };
}

export function isNumber(value: string | number): boolean {
    return !isNaN(Number(value.toString()));
}