import {Pipe, PipeTransform} from '@angular/core';
import {DomSanitizer, SafeHtml} from "@angular/platform-browser";

@Pipe({name: 'highlight'})
export class HighlightDirective implements PipeTransform {

    constructor(public sanitizer: DomSanitizer) {
    }

    transform(text: string, search): SafeHtml {
        if (search && typeof search === 'string') {
            let pattern = search.replace(/[\-\[\]\/\{\}\(\)\*\+\?\.\\\^\$\|]/g, "\\$&");
            pattern = pattern.split(' ').filter((t) => {
                return t.length > 0;
            }).join('|');
            text = text.replace(/\ /g, '&nbsp;');
            let regex = new RegExp(pattern, 'gi');
            return this.sanitizer.bypassSecurityTrustHtml(search ? text.replace(regex, (match) => `<b>${match}</b>`) : text);
        }
        return text;
    }

}
