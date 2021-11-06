import {AfterViewInit, Component, ElementRef, OnInit} from '@angular/core';
import {TranslateService} from "@ngx-translate/core";
import {ActivatedRoute} from "@angular/router";
import {environment} from "@env/environment";
import {ViewportScroller} from "@angular/common";
import {first} from 'rxjs/operators';

@Component({
    selector: 'app-help',
    templateUrl: './help.component.html',
    styleUrls: ['./help.component.css']
})
export class HelpComponent implements OnInit, AfterViewInit {

    lang: string;
    toc: NodeList;
    version: string = environment.version;
    currentSection = 'about';

    constructor(private translate: TranslateService,
                private route: ActivatedRoute,
                private viewportScroller: ViewportScroller,
                private elem: ElementRef) {
    }

    ngOnInit() {
        this.lang = this.translate.currentLang;

    }

    ngAfterViewInit(): void {
        setTimeout(() => {
            this.toc = this.elem.nativeElement.querySelectorAll('h3,h4');
            this.route.fragment.pipe(first()).subscribe(fragment => {
                this.viewportScroller.scrollToAnchor(fragment);
                this.currentSection = fragment;
            });
        }, 0)
    }

    backToTop() {
        this.viewportScroller.scrollToAnchor('top');
    }
}
