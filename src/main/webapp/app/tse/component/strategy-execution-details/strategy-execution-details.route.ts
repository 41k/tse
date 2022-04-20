import { Route } from '@angular/router';
import { StrategyExecutionDetailsComponent } from './strategy-execution-details.component';

export const STRATEGY_EXECUTION_DETAILS_ROUTE: Route = {
  path: '',
  component: StrategyExecutionDetailsComponent,
  data: {
    authorities: [],
    pageTitle: 'strategy-execution-details.title',
  },
};
