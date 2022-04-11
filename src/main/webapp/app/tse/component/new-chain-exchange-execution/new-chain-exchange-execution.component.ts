import { Component } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { Router } from '@angular/router';
import { ChainExchangeExecutionApiClient } from 'app/tse/api-client/chain-exchange-execution.api-client';

@Component({
  selector: 'jhi-new-chain-exchange-execution',
  templateUrl: './new-chain-exchange-execution.component.html',
})
export class NewChainExchangeExecutionComponent {
  assetChains?: Object | null;

  selectedAssetChainId?: string;
  selectedOrderExecutionType?: string;
  amount?: number;
  minProfitThreshold?: number;

  constructor(private chainExchangeExecutionApiClient: ChainExchangeExecutionApiClient, private router: Router) {
    this.initViewData();
  }

  viewDataIsInitialized(): boolean {
    return this.assetChains != null;
  }

  startNewChainExchangeExecution(): void {
    if (!this.selectedAssetChainId || !this.selectedOrderExecutionType || !this.amount || !this.minProfitThreshold) {
      alert('Form is not completed');
      return;
    }
    if (confirm(this.confirmationMessage())) {
      this.chainExchangeExecutionApiClient
        .startChainExchangeExecution({
          assetChainId: +this.selectedAssetChainId,
          orderExecutionType: this.selectedOrderExecutionType,
          amount: this.amount,
          minProfitThreshold: this.minProfitThreshold,
        })
        .subscribe(
          () => {
            this.router.navigate(['chain-exchange-executions']);
          },
          () => alert('FAILED to start new chain exchange execution')
        );
    }
  }

  getAssetChainIds(): Array<string> {
    return Object.keys(this.assetChains!);
  }

  formAssetChainString(assetChainId: string): string {
    return Array.from(this.assetChains![assetChainId]).join(' / ');
  }

  private confirmationMessage(): string {
    let message = '\n[SETTINGS]';
    message += '\nAsset chain: ' + this.formAssetChainString(this.selectedAssetChainId!);
    message += '\nOrder execution type: ' + this.selectedOrderExecutionType;
    message += '\nAmount: ' + this.amount;
    message += '\nMin profit threshold: ' + this.minProfitThreshold;
    return message;
  }

  private initViewData(): void {
    this.chainExchangeExecutionApiClient.getAssetChains().subscribe((response: HttpResponse<Object>) => {
      this.assetChains = response.body;
    });
  }
}
