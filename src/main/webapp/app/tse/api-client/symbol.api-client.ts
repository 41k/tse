import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { SERVER_API_URL } from 'app/app.constants';

@Injectable({ providedIn: 'root' })
export class SymbolApiClient {
  constructor(private http: HttpClient) {}

  getSymbols(): Observable<HttpResponse<string[]>> {
    return this.http.get<string[]>(SERVER_API_URL + '/api/v1/symbols', { observe: 'response' });
  }
}
