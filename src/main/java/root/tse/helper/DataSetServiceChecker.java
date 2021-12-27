package root.tse.helper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import root.tse.domain.backtest.DataSetService;
import root.tse.domain.clock.Interval;

//@Component
@Slf4j
@RequiredArgsConstructor
public class DataSetServiceChecker implements CommandLineRunner {

    private final DataSetService dataSetService;

    @Override
    public void run(String... args) {
        var dataSetName = "data_set_1";
        var symbol = "ETH_USD";
        System.out.println("----------------------------");
        System.out.println("start timestamp: " + dataSetService.getStartTimestamp(dataSetName, symbol));
        System.out.println("end timestamp: " + dataSetService.getEndTimestamp(dataSetName, symbol));
        System.out.println("----------------------------");
        var series = dataSetService.getSeries(dataSetName, symbol, Interval.ONE_MINUTE, 1621613580000L, 5);
        series.getBarData().forEach(bar -> System.out.println("bar close price: " + bar.getClosePrice()));
        System.out.println("----------------------------");
    }
}
