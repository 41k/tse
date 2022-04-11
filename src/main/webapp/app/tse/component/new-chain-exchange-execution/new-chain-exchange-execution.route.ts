import { Route } from '@angular/router';
import { NewChainExchangeExecutionComponent } from './new-chain-exchange-execution.component';

export const NEW_CHAIN_EXCHANGE_EXECUTION_ROUTE: Route = {
  path: '',
  component: NewChainExchangeExecutionComponent,
  data: {
    authorities: [],
    pageTitle: 'global.title',
  },
};
