package root.tse.presentation;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import root.tse.domain.backtest.BacktestService;
import root.tse.domain.strategy_execution.report.Report;

@RestController
@RequestMapping("/api/v1/backtest")
@RequiredArgsConstructor
public class BacktestController {

    private final BacktestService backtestService;

    @GetMapping("/report")
    public Report getReport() {
        return backtestService.getReport();
    }
}
