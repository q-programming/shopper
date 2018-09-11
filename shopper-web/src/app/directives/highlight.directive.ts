import {Pipe, PipeTransform} from '@angular/core';

@Pipe({name: 'highlight'})
export class HighlightDirective implements PipeTransform {

    transform(text: string, search): string {
        let pattern = search.replace(/[\-\[\]\/\{\}\(\)\*\+\?\.\\\^\$\|]/g, "\\$&");
        pattern = pattern.split(' ').filter((t) => {
            return t.length > 0;
        }).join('|');
        let regex = new RegExp(pattern, 'gi');
        return search ? text.replace(regex, (match) => `<b>${match}</b>`) : text;
    }

}
