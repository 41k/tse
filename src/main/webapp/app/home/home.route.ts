import { Route } from '@angular/router';

import { SignInComponent } from '../shared/sign-in/sign-in.component';

export const HOME_ROUTE: Route = {
  path: '',
  component: SignInComponent,
  data: {
    authorities: [],
    pageTitle: 'login.form.button',
  },
};
