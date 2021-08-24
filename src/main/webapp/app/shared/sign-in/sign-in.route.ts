import { Route } from '@angular/router';
import { SignInComponent } from './sign-in.component';

export const SIGN_IN_ROUTE: Route = {
  path: '',
  component: SignInComponent,
  data: {
    authorities: [],
    pageTitle: 'sign-in.title',
  },
};
