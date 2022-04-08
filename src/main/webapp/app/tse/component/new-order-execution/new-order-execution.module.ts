import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { TseSharedModule } from 'app/shared/shared.module';
import { NEW_ORDER_EXECUTION_ROUTE } from './new-order-execution.route';
import { NewOrderExecutionComponent } from './new-order-execution.component';

@NgModule({
  imports: [TseSharedModule, RouterModule.forChild([NEW_ORDER_EXECUTION_ROUTE])],
  declarations: [NewOrderExecutionComponent],
})
export class NewOrderExecutionModule {}
