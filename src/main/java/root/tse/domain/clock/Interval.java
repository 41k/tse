package root.tse.domain.clock;

public enum Interval {

    ONE_MINUTE(MillisOf.ONE_MINUTE),
    THREE_MINUTES(3 * MillisOf.ONE_MINUTE),
    FIVE_MINUTES(5 * MillisOf.ONE_MINUTE),
    FIFTEEN_MINUTES(15 * MillisOf.ONE_MINUTE),
    THIRTY_MINUTES(30 * MillisOf.ONE_MINUTE),

    ONE_HOUR(MillisOf.ONE_HOUR),
    TWO_HOURS(2 * MillisOf.ONE_HOUR),
    FOUR_HOURS(4 * MillisOf.ONE_HOUR),
    SIX_HOURS(6 * MillisOf.ONE_HOUR),
    EIGHT_HOURS(8 * MillisOf.ONE_HOUR),
    TWELVE_HOURS(12 * MillisOf.ONE_HOUR),

    ONE_DAY(MillisOf.ONE_DAY),
    THREE_DAYS(3 * MillisOf.ONE_DAY);

    private final long millis;

    Interval(long millis) {
        this.millis = millis;
    }

    public long inMillis() {
        return millis;
    }

    private static class MillisOf {
        public static final long ONE_MINUTE = 60_000L;
        public static final long ONE_HOUR = 60 * ONE_MINUTE;
        public static final long ONE_DAY = 24 * ONE_HOUR;
    }
}
