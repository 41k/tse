import { Component, ViewChild } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpResponse } from '@angular/common/http';
import { DatePipe } from '@angular/common';
import { ChartComponent } from 'ng-apexcharts';
import { StrategyExecutionApiClient } from 'app/tse/api-client/strategy-execution.api-client';
import { StrategyExecution } from 'app/tse/model/strategy-execution.model';
import { Report } from 'app/tse/model/report.model';

@Component({
  selector: 'jhi-strategy-execution-details',
  templateUrl: './strategy-execution-details.component.html',
})
export class StrategyExecutionDetailsComponent {
  chartTimeFormat = 'yyyy-MM-dd HH:mm';
  fractionDigitsCount = 5;

  strategyExecutionId: string;
  strategyExecution?: StrategyExecution | null;
  report?: Report | null;

  @ViewChild('chart') chart!: ChartComponent;
  chartOptions: Partial<any>;

  constructor(
    private activatedRoute: ActivatedRoute,
    private strategyExecutionApiClient: StrategyExecutionApiClient,
    private datePipe: DatePipe,
    private router: Router
  ) {
    this.strategyExecutionId = this.activatedRoute.snapshot.params['id'];
    this.chartOptions = this.getInitialChartOptions();
    this.initViewData();
  }

  private initViewData(): void {
    this.strategyExecutionApiClient
      .getStrategyExecution(this.strategyExecutionId)
      .subscribe((response: HttpResponse<StrategyExecution>) => {
        this.strategyExecution = response.body;
      });
    this.strategyExecutionApiClient.getReport(this.strategyExecutionId).subscribe((response: HttpResponse<Report>) => {
      this.report = response.body;
      if (this.report) {
        this.initChart();
      }
    });
  }

  stopStrategyExecution(): void {
    if (confirm(`Do you want to stop strategy execution [${this.strategyExecutionId}] ?`)) {
      this.strategyExecutionApiClient.stopStrategyExecution(this.strategyExecutionId).subscribe(
        () => {
          alert(`Strategy execution [${this.strategyExecutionId}] has been stopped successfully`);
          this.router.navigate(['strategy-executions']);
        },
        () => alert('Failed to stop strategy execution')
      );
    }
  }

  viewDataIsInitialized(): boolean {
    return this.strategyExecution != null && this.report != null;
  }

  private getInitialChartOptions(): Partial<any> {
    return {
      series: [
        {
          name: 'Total profit',
          data: [],
        },
      ],
      chart: {
        type: 'area',
        height: 180,
        zoom: {
          enabled: false,
        },
        toolbar: {
          show: false,
        },
      },
      dataLabels: {
        enabled: false,
      },
      stroke: {
        curve: 'straight',
        width: 2,
      },
      labels: [],
      xaxis: {
        type: 'category',
        labels: {
          show: false,
        },
      },
      yaxis: {
        opposite: true,
      },
      legend: {
        horizontalAlign: 'left',
      },
    };
  }

  private initChart(): void {
    const equityCurvePoints = this.report?.equityCurve;
    if (equityCurvePoints && equityCurvePoints.length > 0) {
      const data = [];
      const labels = [];
      for (let i = 0; i < equityCurvePoints.length; i++) {
        const point = equityCurvePoints[i];
        const amount = this.formatFractionDigits(point.amount);
        data.push(amount);
        const label = this.datePipe.transform(new Date(point.timestamp).getTime(), this.chartTimeFormat)!;
        labels.push(label);
      }
      this.chartOptions.series[0].data = data;
      this.chartOptions.labels = labels;
    }
  }

  private formatFractionDigits(num: number): number {
    return Number(num.toFixed(this.fractionDigitsCount));
  }
}
