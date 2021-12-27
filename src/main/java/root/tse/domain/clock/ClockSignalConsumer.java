package root.tse.domain.clock;

public interface ClockSignalConsumer {

    String getId();

    void accept(ClockSignal clockSignal);
}
