import { Route } from '@angular/router';
import { BacktestReportComponent } from 'app/tse/component/backtest-report/backtest-report.component';

export const BACKTEST_REPORT_ROUTE: Route = {
  path: '',
  component: BacktestReportComponent,
  data: {
    authorities: [],
    pageTitle: 'backtest-report.title',
  },
};
