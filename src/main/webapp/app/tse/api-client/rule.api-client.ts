import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { SERVER_API_URL } from 'app/app.constants';
import { Rule } from 'app/tse/model/rule.model';

@Injectable({ providedIn: 'root' })
export class RuleApiClient {
  private baseUrl = SERVER_API_URL + '/api/v1/rules';

  constructor(private http: HttpClient) {}

  getEntryRules(): Observable<HttpResponse<Rule[]>> {
    return this.http.get<Rule[]>(this.baseUrl + '/entry', { observe: 'response' });
  }

  getExitRules(): Observable<HttpResponse<Rule[]>> {
    return this.http.get<Rule[]>(this.baseUrl + '/exit', { observe: 'response' });
  }
}
