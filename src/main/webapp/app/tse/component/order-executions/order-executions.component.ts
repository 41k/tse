import { Component } from '@angular/core';
import { OrderExecutionApiClient } from 'app/tse/api-client/order-execution.api-client';
import { OrderExecution } from 'app/tse/model/order-execution.model';
import { HttpResponse } from '@angular/common/http';
import { DatePipe } from '@angular/common';

@Component({
  selector: 'jhi-order-executions',
  templateUrl: './order-executions.component.html',
})
export class OrderExecutionsComponent {
  timeFormat = 'yyyy-MM-dd HH:mm:ss';
  orderExecutions?: OrderExecution[] | null;

  constructor(private orderExecutionApiClient: OrderExecutionApiClient, private datePipe: DatePipe) {
    this.initViewData();
  }

  private initViewData(): void {
    this.orderExecutionApiClient.getOrderExecutions().subscribe((response: HttpResponse<OrderExecution[]>) => {
      this.orderExecutions = response.body;
    });
  }

  stopOrderExecution(orderExecutionId: string): void {
    if (confirm(`Do you want to stop order execution [${orderExecutionId}] ?`)) {
      this.orderExecutionApiClient.stopOrderExecution(orderExecutionId).subscribe(
        () => {
          alert(`Order execution [${orderExecutionId}] has been stopped successfully`);
          this.initViewData();
        },
        () => alert('Failed to stop order execution')
      );
    }
  }

  getExecutionTime(orderExecution: OrderExecution): string {
    return this.datePipe.transform(new Date(orderExecution.timestamp).getTime(), this.timeFormat)!;
  }
}
