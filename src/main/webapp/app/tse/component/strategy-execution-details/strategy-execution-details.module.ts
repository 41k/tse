import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { TseSharedModule } from 'app/shared/shared.module';
import { STRATEGY_EXECUTION_DETAILS_ROUTE } from './strategy-execution-details.route';
import { StrategyExecutionDetailsComponent } from './strategy-execution-details.component';
import { NgApexchartsModule } from 'ng-apexcharts';

@NgModule({
  imports: [TseSharedModule, NgApexchartsModule, RouterModule.forChild([STRATEGY_EXECUTION_DETAILS_ROUTE])],
  declarations: [StrategyExecutionDetailsComponent],
})
export class StrategyExecutionDetailsModule {}
