import { Component, ViewChild } from '@angular/core';
import { DatePipe } from '@angular/common';
import { HttpResponse } from '@angular/common/http';
import { Report } from 'app/tse/model/report.model';
import { BacktestApiClient } from 'app/tse/api-client/backtest.api-client';
import { ChartComponent } from 'ng-apexcharts';

@Component({
  selector: 'jhi-backtest-report',
  templateUrl: './backtest-report.component.html',
})
export class BacktestReportComponent {
  private chartTimeFormat = 'yyyy-MM-dd HH:mm';
  private fractionDigitsCount = 5;

  report?: Report | null;

  @ViewChild('chart') chart!: ChartComponent;
  chartOptions: Partial<any>;

  constructor(private backtestApiClient: BacktestApiClient, private datePipe: DatePipe) {
    this.chartOptions = this.getInitialChartOptions();
    this.initViewData();
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
        height: 330,
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

  private initViewData(): void {
    this.backtestApiClient.getReport().subscribe((response: HttpResponse<Report>) => {
      this.report = response.body;
      if (this.report) {
        this.initChart();
      }
    });
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
