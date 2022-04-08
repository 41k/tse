import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { SERVER_API_URL } from 'app/app.constants';
import { OrderExecution } from '../model/order-execution.model';

export interface StartOrderExecutionRequest {
  orderExecutionType: string;
  orderType: string;
  symbol: string;
  amount: number;
  ruleId: string;
  ruleParameters: object;
}

@Injectable({ providedIn: 'root' })
export class OrderExecutionApiClient {
  private baseUrl = SERVER_API_URL + '/api/v1/order-executions';

  constructor(private http: HttpClient) {}

  getOrderExecutions(): Observable<HttpResponse<OrderExecution[]>> {
    return this.http.get<OrderExecution[]>(this.baseUrl, { observe: 'response' });
  }

  startOrderExecution(requestBody: StartOrderExecutionRequest): Observable<{}> {
    return this.http.post(this.baseUrl, requestBody);
  }

  stopOrderExecution(orderExecutionId: string): Observable<{}> {
    return this.http.delete(this.baseUrl + `/${orderExecutionId}`);
  }
}
