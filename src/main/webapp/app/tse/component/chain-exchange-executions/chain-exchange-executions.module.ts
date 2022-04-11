import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { TseSharedModule } from 'app/shared/shared.module';
import { CHAIN_EXCHANGE_EXECUTIONS_ROUTE } from './chain-exchange-executions.route';
import { ChainExchangeExecutionsComponent } from './chain-exchange-executions.component';

@NgModule({
  imports: [TseSharedModule, RouterModule.forChild([CHAIN_EXCHANGE_EXECUTIONS_ROUTE])],
  declarations: [ChainExchangeExecutionsComponent],
})
export class ChainExchangeExecutionsModule {}
