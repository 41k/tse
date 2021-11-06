import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { SERVER_API_URL } from 'app/app.constants';
import { Report } from 'app/tse/model/report.model';

@Injectable({ providedIn: 'root' })
export class BacktestApiClient {
  baseUrl!: string;

  constructor(private http: HttpClient) {
    this.baseUrl = SERVER_API_URL + '/api/backtest';
  }

  getReport(): Observable<HttpResponse<Report>> {
    const requestURL = this.baseUrl + '/report';
    return this.http.get<Report>(requestURL, {
      observe: 'response',
    });
  }
}
