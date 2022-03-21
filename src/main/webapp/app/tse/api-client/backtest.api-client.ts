import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { SERVER_API_URL } from 'app/app.constants';
import { Report } from 'app/tse/model/report.model';

@Injectable({ providedIn: 'root' })
export class BacktestApiClient {
  constructor(private http: HttpClient) {}

  getReport(): Observable<HttpResponse<Report>> {
    return this.http.get<Report>(SERVER_API_URL + '/api/v1/backtest/report', { observe: 'response' });
  }
}
