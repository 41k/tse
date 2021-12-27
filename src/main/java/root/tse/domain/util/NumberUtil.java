package root.tse.domain.util;

import lombok.experimental.UtilityClass;

import java.text.DecimalFormat;

@UtilityClass
public class NumberUtil {

    public static double trimToPrecision(double value, int precision) {
        var precisionFormatter = new DecimalFormat("#." + "#".repeat(Math.max(0, precision)));
        if (Double.parseDouble(precisionFormatter.format(value)) == value) {
            return value;
        }
        var trimmedValue = value - (value % Math.pow(10, -precision));
        return Double.parseDouble(precisionFormatter.format(trimmedValue));
    }
}
