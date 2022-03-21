import { Route } from '@angular/router';
import { NewStrategyExecutionComponent } from './new-strategy-execution.component';

export const NEW_STRATEGY_EXECUTION_ROUTE: Route = {
  path: '',
  component: NewStrategyExecutionComponent,
  data: {
    authorities: [],
    pageTitle: 'global.title',
  },
};
