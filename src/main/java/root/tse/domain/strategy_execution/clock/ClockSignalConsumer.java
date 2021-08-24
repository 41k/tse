package root.tse.domain.strategy_execution.clock;

public interface ClockSignalConsumer {

    String getId();
    void acceptClockSignal();
}
