import {Component, ElementRef, EventEmitter, HostListener, Inject, Input, Output} from '@angular/core';
import {DOCUMENT} from "@angular/common";

@Component({
    selector: 'pull-to-refresh',
    templateUrl: './pull-to-refresh.component.html',
    styles: []
})
export class PullToRefreshComponent {

    private lastScrollTop: number = 0;
    private isAtTop: boolean = false;
    private element: any;

    @Input('refresh') inProgress: boolean = false;
    @Output() onPull: EventEmitter<any> = new EventEmitter<any>();

    constructor(el: ElementRef, @Inject(DOCUMENT) private document: Document) {
        this.element = el.nativeElement;
    }

    private get scrollTop() {
        return this.element.scrollTop || 0;
    }

    @HostListener('scroll')
    @HostListener('touchmove')
    onScroll() {
        console.log("window offset:" + window.pageYOffset);
        console.log("window scrollY:" + window.scrollY);
        console.log("el:" + this.scrollTop);
        console.log("doc" + this.document.scrollingElement.scrollTop);

        if (this.scrollTop <= 0 && this.lastScrollTop <= 0) {
            if (this.isAtTop) {
                this.onPull.emit(true);
            }
            else this.isAtTop = true;
        }
        this.lastScrollTop = this.scrollTop;
    }

}
