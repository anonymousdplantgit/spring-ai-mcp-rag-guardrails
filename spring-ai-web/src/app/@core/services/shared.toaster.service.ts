import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class SharedToasterService {
  actionSuccess = new Subject<string>();
  actionWarning = new Subject<string>();
  actionError = new Subject<string>();
  constructor() {}
}
