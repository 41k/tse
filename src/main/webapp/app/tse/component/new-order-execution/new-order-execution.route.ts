import { Route } from '@angular/router';
import { NewOrderExecutionComponent } from './new-order-execution.component';

export const NEW_ORDER_EXECUTION_ROUTE: Route = {
  path: '',
  component: NewOrderExecutionComponent,
  data: {
    authorities: [],
    pageTitle: 'new-order-execution.title',
  },
};
