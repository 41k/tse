import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { TseSharedModule } from 'app/shared/shared.module';
import { NEW_STRATEGY_EXECUTION_ROUTE } from './new-strategy-execution.route';
import { NewStrategyExecutionComponent } from './new-strategy-execution.component';

@NgModule({
  imports: [TseSharedModule, RouterModule.forChild([NEW_STRATEGY_EXECUTION_ROUTE])],
  declarations: [NewStrategyExecutionComponent],
})
export class NewStrategyExecutionModule {}
