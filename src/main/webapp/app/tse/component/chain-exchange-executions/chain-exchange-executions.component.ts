import { Component } from '@angular/core';
import { ChainExchangeExecutionApiClient } from 'app/tse/api-client/chain-exchange-execution.api-client';
import { ChainExchangeExecution } from 'app/tse/model/chain-exchange-execution.model';
import { HttpResponse } from '@angular/common/http';

@Component({
  selector: 'jhi-chain-exchange-executions',
  templateUrl: './chain-exchange-executions.component.html',
})
export class ChainExchangeExecutionsComponent {
  chainExchangeExecutions?: ChainExchangeExecution[] | null;

  constructor(private chainExchangeExecutionApiClient: ChainExchangeExecutionApiClient) {
    this.initViewData();
  }

  private initViewData(): void {
    this.chainExchangeExecutionApiClient.getChainExchangeExecutions().subscribe((response: HttpResponse<ChainExchangeExecution[]>) => {
      this.chainExchangeExecutions = response.body;
    });
  }

  stopChainExchangeExecution(chainExchangeExecution: ChainExchangeExecution): void {
    const assetChain = this.formAssetChainString(chainExchangeExecution);
    if (confirm(`Do you want to stop chain exchange execution [${assetChain}] ?`)) {
      this.chainExchangeExecutionApiClient.stopChainExchangeExecution(chainExchangeExecution.assetChainId).subscribe(
        () => {
          alert(`Chain exchange execution [${assetChain}] has been stopped successfully`);
          this.initViewData();
        },
        () => alert('Failed to stop chain exchange execution')
      );
    }
  }

  formAssetChainString(chainExchangeExecution: ChainExchangeExecution): string {
    return chainExchangeExecution.assetChain.join(' / ');
  }
}
