import { Route } from '@angular/router';
import { StrategyExecutionsComponent } from './strategy-executions.component';

export const STRATEGY_EXECUTIONS_ROUTE: Route = {
  path: '',
  component: StrategyExecutionsComponent,
  data: {
    authorities: [],
    pageTitle: 'strategy-executions.title',
  },
};
