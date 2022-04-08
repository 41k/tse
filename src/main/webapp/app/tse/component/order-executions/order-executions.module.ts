import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { TseSharedModule } from 'app/shared/shared.module';
import { ORDER_EXECUTIONS_ROUTE } from './order-executions.route';
import { OrderExecutionsComponent } from './order-executions.component';

@NgModule({
  imports: [TseSharedModule, RouterModule.forChild([ORDER_EXECUTIONS_ROUTE])],
  declarations: [OrderExecutionsComponent],
})
export class OrderExecutionsModule {}
