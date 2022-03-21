import { Component } from '@angular/core';
import { StrategyExecutionApiClient } from 'app/tse/api-client/strategy-execution.api-client';
import { StrategyExecution } from 'app/tse/model/strategy-execution.model';
import { HttpResponse } from '@angular/common/http';

@Component({
  selector: 'jhi-strategy-executions',
  templateUrl: './strategy-executions.component.html',
})
export class StrategyExecutionsComponent {
  strategyExecutions?: StrategyExecution[] | null;

  constructor(private strategyExecutionApiClient: StrategyExecutionApiClient) {
    this.initViewData();
  }

  private initViewData(): void {
    this.strategyExecutionApiClient.getStrategyExecutions().subscribe((response: HttpResponse<StrategyExecution[]>) => {
      this.strategyExecutions = response.body;
    });
  }

  stopStrategyExecution(strategyExecutionId: string): void {
    if (confirm(`Do you want to stop strategy execution [${strategyExecutionId}] ?`)) {
      this.strategyExecutionApiClient.stopStrategyExecution(strategyExecutionId).subscribe(
        () => {
          alert(`Strategy execution [${strategyExecutionId}] has been stopped successfully`);
          this.initViewData();
        },
        () => alert('Failed to stop strategy execution')
      );
    }
  }
}
