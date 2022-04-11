import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { SERVER_API_URL } from 'app/app.constants';
import { ChainExchangeExecution } from '../model/chain-exchange-execution.model';

export interface StartChainExchangeExecutionRequest {
  assetChainId: number;
  orderExecutionType: string;
  amount: number;
  minProfitThreshold: number;
}

@Injectable({ providedIn: 'root' })
export class ChainExchangeExecutionApiClient {
  private baseUrl = SERVER_API_URL + '/api/v1/chain-exchange-executions';

  constructor(private http: HttpClient) {}

  getAssetChains(): Observable<HttpResponse<Object>> {
    return this.http.get<Map<number, Array<string>>>(this.baseUrl + '/asset-chains', { observe: 'response' });
  }

  getChainExchangeExecutions(): Observable<HttpResponse<ChainExchangeExecution[]>> {
    return this.http.get<ChainExchangeExecution[]>(this.baseUrl, { observe: 'response' });
  }

  startChainExchangeExecution(requestBody: StartChainExchangeExecutionRequest): Observable<{}> {
    return this.http.post(this.baseUrl, requestBody);
  }

  stopChainExchangeExecution(assetChainId: number): Observable<{}> {
    return this.http.delete(this.baseUrl + `/${assetChainId}`);
  }
}
