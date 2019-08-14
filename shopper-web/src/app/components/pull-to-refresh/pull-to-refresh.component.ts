import {Component, ElementRef, EventEmitter, HostListener, Inject, Input, Output} from '@angular/core';
import {DOCUMENT} from "@angular/common";

@Component({
    selector: 'pull-to-refresh',
    templateUrl: './pull-to-refresh.component.html',
    styleUrls: ['./pull-to-refresh.component.css']
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
        return this.document.scrollingElement.scrollTop || 0;
    }

    @HostListener('scroll')
    @HostListener('touchmove')
    onScroll() {
        if (this.scrollTop <= 0 && this.lastScrollTop <= 0) {
            if (this.isAtTop && !this.inProgress) {
                this.onPull.emit(true);
                this.inProgress = true
            } else this.isAtTop = true;
        }
        this.lastScrollTop = this.scrollTop;
    }

}
