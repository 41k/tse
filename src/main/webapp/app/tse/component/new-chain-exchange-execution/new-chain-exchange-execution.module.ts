import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { TseSharedModule } from 'app/shared/shared.module';
import { NEW_CHAIN_EXCHANGE_EXECUTION_ROUTE } from './new-chain-exchange-execution.route';
import { NewChainExchangeExecutionComponent } from './new-chain-exchange-execution.component';

@NgModule({
  imports: [TseSharedModule, RouterModule.forChild([NEW_CHAIN_EXCHANGE_EXECUTION_ROUTE])],
  declarations: [NewChainExchangeExecutionComponent],
})
export class NewChainExchangeExecutionModule {}
