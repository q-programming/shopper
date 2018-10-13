import {Pipe, PipeTransform} from '@angular/core';
import {DomSanitizer, SafeHtml} from "@angular/platform-browser";

@Pipe({name: 'trim'})
export class TrimDirective implements PipeTransform {

    constructor(public sanitizer: DomSanitizer) {
    }

    transform(text: string, maxLenght: number): SafeHtml {
        if (!text || text.length < maxLenght) {
            return this.sanitizer.bypassSecurityTrustHtml(`<span>${text}</span>`);
        }
        let trimmed = text.slice(0, maxLenght - 2);
        return this.sanitizer.bypassSecurityTrustHtml(`<span matTooltip="${text}">${trimmed}...</span>`);
    }

}
