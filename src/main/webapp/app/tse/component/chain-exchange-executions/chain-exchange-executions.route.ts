import { Route } from '@angular/router';
import { ChainExchangeExecutionsComponent } from './chain-exchange-executions.component';

export const CHAIN_EXCHANGE_EXECUTIONS_ROUTE: Route = {
  path: '',
  component: ChainExchangeExecutionsComponent,
  data: {
    authorities: [],
    pageTitle: 'global.title',
  },
};
