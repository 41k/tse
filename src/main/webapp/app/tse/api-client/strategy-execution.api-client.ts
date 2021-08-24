import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { SERVER_API_URL } from 'app/app.constants';
import { StrategyExecution } from '../model/strategy-execution.model';

@Injectable({ providedIn: 'root' })
export class StrategyExecutionApiClient {
  constructor(private http: HttpClient) {}

  getStrategyExecutions(): Observable<HttpResponse<StrategyExecution[]>> {
    const requestURL = SERVER_API_URL + '/api/strategy-executions/';
    return this.http.get<StrategyExecution[]>(requestURL, {
      observe: 'response',
    });
  }
}
