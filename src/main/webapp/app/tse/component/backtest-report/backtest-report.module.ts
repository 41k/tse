import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { TseSharedModule } from 'app/shared/shared.module';
import { BACKTEST_REPORT_ROUTE } from './backtest-report.route';
import { BacktestReportComponent } from 'app/tse/component/backtest-report/backtest-report.component';
import { NgApexchartsModule } from 'ng-apexcharts';

@NgModule({
  imports: [TseSharedModule, NgApexchartsModule, RouterModule.forChild([BACKTEST_REPORT_ROUTE])],
  declarations: [BacktestReportComponent],
})
export class BacktestReportModule {}
