import {AfterViewInit, Component, ElementRef, OnInit} from '@angular/core';
import {TranslateService} from "@ngx-translate/core";
import {ActivatedRoute} from "@angular/router";
import {ScrollToConfigOptions, ScrollToService} from "@nicky-lenaers/ngx-scroll-to";

@Component({
    selector: 'app-help',
    templateUrl: './help.component.html',
    styleUrls: ['./help.component.css']
})
export class HelpComponent implements OnInit, AfterViewInit {

    lang: string;
    toc: NodeList;

    constructor(private translate: TranslateService,
                private route: ActivatedRoute,
                private scrollToService: ScrollToService,
                private elem: ElementRef) {
    }

    ngOnInit() {
        this.lang = this.translate.currentLang;

    }

    ngAfterViewInit(): void {
        setTimeout(() => {
            this.toc = this.elem.nativeElement.querySelectorAll('h3,h4');
        }, 0)
    }

    backToTop() {
        const config: ScrollToConfigOptions = {
            target: 'top',
            offset: -70
        };
        this.scrollToService.scrollTo(config);
    }
}
