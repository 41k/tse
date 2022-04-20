import { Route } from '@angular/router';
import { OrderExecutionsComponent } from './order-executions.component';

export const ORDER_EXECUTIONS_ROUTE: Route = {
  path: '',
  component: OrderExecutionsComponent,
  data: {
    authorities: [],
    pageTitle: 'order-executions.title',
  },
};
