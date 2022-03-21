import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { SERVER_API_URL } from 'app/app.constants';
import { StrategyExecution } from '../model/strategy-execution.model';
import { Report } from 'app/tse/model/report.model';

export interface StartStrategyExecutionRequest {
  orderExecutionType: string;
  symbol: string;
  fundsPerTrade: number;
  entryRuleId: string;
  exitRuleId: string;
  entryRuleParameters: object;
  exitRuleParameters: object;
}

@Injectable({ providedIn: 'root' })
export class StrategyExecutionApiClient {
  private baseUrl = SERVER_API_URL + '/api/v1/strategy-executions';

  constructor(private http: HttpClient) {}

  getStrategyExecutions(): Observable<HttpResponse<StrategyExecution[]>> {
    return this.http.get<StrategyExecution[]>(this.baseUrl, { observe: 'response' });
  }

  getStrategyExecution(strategyExecutionId: string): Observable<HttpResponse<StrategyExecution>> {
    return this.http.get<StrategyExecution>(this.baseUrl + `/${strategyExecutionId}`, { observe: 'response' });
  }

  startStrategyExecution(requestBody: StartStrategyExecutionRequest): Observable<{}> {
    return this.http.post(this.baseUrl, requestBody);
  }

  stopStrategyExecution(strategyExecutionId: string): Observable<{}> {
    return this.http.delete(this.baseUrl + `/${strategyExecutionId}`);
  }

  getReport(strategyExecutionId: string): Observable<HttpResponse<Report>> {
    return this.http.get<Report>(this.baseUrl + `/${strategyExecutionId}/report`, { observe: 'response' });
  }
}
