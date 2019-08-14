import {Component, EventEmitter, Input, Output} from '@angular/core';

@Component({
    selector: 'backdrop',
    templateUrl: './backdrop.component.html',
    styleUrls: ['./backdrop.component.css']
})
export class BackdropComponent {

    @Input() show: boolean;
    @Output() clicked = new EventEmitter<boolean>();

    constructor() {
    }

    click() {
        this.clicked.emit(true);
    }

}
