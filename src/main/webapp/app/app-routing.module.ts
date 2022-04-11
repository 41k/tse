import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { errorRoute } from './layouts/error/error.route';
import { navbarRoute } from './layouts/navbar/navbar.route';
import { DEBUG_INFO_ENABLED } from 'app/app.constants';
import { Authority } from 'app/shared/constants/authority.constants';

import { UserRouteAccessService } from 'app/core/auth/user-route-access-service';

const LAYOUT_ROUTES = [navbarRoute, ...errorRoute];

@NgModule({
  imports: [
    RouterModule.forRoot(
      [
        {
          path: 'admin',
          data: {
            authorities: [Authority.ADMIN],
          },
          canActivate: [UserRouteAccessService],
          loadChildren: () => import('./admin/admin-routing.module').then(m => m.AdminRoutingModule),
        },
        {
          path: 'account',
          loadChildren: () => import('./account/account.module').then(m => m.AccountModule),
        },
        {
          path: 'sign-in',
          loadChildren: () => import('./shared/sign-in/sign-in.module').then(m => m.SignInModule),
        },
        {
          path: 'strategy-executions',
          loadChildren: () =>
            import('./tse/component/strategy-executions/strategy-executions.module').then(m => m.StrategyExecutionsModule),
        },
        {
          path: 'strategy-executions/:id',
          loadChildren: () =>
            import('./tse/component/strategy-execution-details/strategy-execution-details.module').then(
              m => m.StrategyExecutionDetailsModule
            ),
        },
        {
          path: 'new-strategy-execution',
          loadChildren: () =>
            import('./tse/component/new-strategy-execution/new-strategy-execution.module').then(m => m.NewStrategyExecutionModule),
        },
        {
          path: 'backtest-report',
          loadChildren: () => import('./tse/component/backtest-report/backtest-report.module').then(m => m.BacktestReportModule),
        },
        {
          path: 'order-executions',
          loadChildren: () => import('./tse/component/order-executions/order-executions.module').then(m => m.OrderExecutionsModule),
        },
        {
          path: 'new-order-execution',
          loadChildren: () => import('./tse/component/new-order-execution/new-order-execution.module').then(m => m.NewOrderExecutionModule),
        },
        {
          path: 'chain-exchange-executions',
          loadChildren: () =>
            import('./tse/component/chain-exchange-executions/chain-exchange-executions.module').then(m => m.ChainExchangeExecutionsModule),
        },
        {
          path: 'new-chain-exchange-execution',
          loadChildren: () =>
            import('./tse/component/new-chain-exchange-execution/new-chain-exchange-execution.module').then(
              m => m.NewChainExchangeExecutionModule
            ),
        },
        ...LAYOUT_ROUTES,
      ],
      { enableTracing: DEBUG_INFO_ENABLED }
    ),
  ],
  exports: [RouterModule],
})
export class TseAppRoutingModule {}
