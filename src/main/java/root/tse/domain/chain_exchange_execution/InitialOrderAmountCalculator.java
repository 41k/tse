package root.tse.domain.chain_exchange_execution;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import root.tse.domain.order.OrderType;

import java.util.Map;
import java.util.Optional;

import static root.tse.domain.util.NumberUtil.trimToPrecision;

// Note: it is necessary to minimize Order3 amount precision remainder by selecting appropriate amount for Order1
@Slf4j
@RequiredArgsConstructor
public class InitialOrderAmountCalculator {

    public Optional<Double> tryToCalculate(ChainExchangeExecutionContext context, Map<String, Map<OrderType, Double>> prices) {
        var exchangeAmount = context.getAmount();
        var orderFeePercent = context.getOrderFeePercent();
        var nSteps = context.getNAmountSelectionSteps();
        var symbols = context.getSymbols();
        var symbolToPrecisionMap = context.getSymbolToPrecisionMap();
        var symbol1 = symbols.get(0);
        var priceForSymbol1 = prices.get(symbol1).get(OrderType.BUY);
        var precisionForSymbol1 = symbolToPrecisionMap.get(symbol1);
        var minAmountForOrder1 = calculateMinAmount(precisionForSymbol1);
        var amountForOrder1 = exchangeAmount / priceForSymbol1;
        if (amountForOrder1 < minAmountForOrder1) {
            log.warn(">>> amountForOrder1[{}] < minAmountForOrder1[{}]", amountForOrder1, minAmountForOrder1);
            return Optional.empty();
        }
        amountForOrder1 = trimToPrecision(amountForOrder1, precisionForSymbol1);
        var symbol2 = symbols.get(1);
        var priceForSymbol2 = prices.get(symbol2).get(OrderType.SELL);
        var symbol3 = symbols.get(2);
        var precisionForSymbol3 = symbolToPrecisionMap.get(symbol3);
        var minAmountForOrder3 = calculateMinAmount(precisionForSymbol3);
        var minRemainder = Double.MAX_VALUE;
        var targetAmount = amountForOrder1;
        for (var i = 0; i < nSteps; i++) {
            var amountForOrder2 = amountForOrder1;
            var amountForOrder3 = (amountForOrder2 * priceForSymbol2) - (amountForOrder2 * priceForSymbol2 * orderFeePercent / 100);
            if (amountForOrder3 < minAmountForOrder3) {
                if (i == 0) {
                    log.warn(">>> amountForOrder3[{}] < minAmountForOrder3[{}]", amountForOrder3, minAmountForOrder3);
                    return Optional.empty();
                }
                break;
            }
            var remainder = amountForOrder3 - trimToPrecision(amountForOrder3, precisionForSymbol3);
            if (remainder < minRemainder) {
                minRemainder = remainder;
                targetAmount = amountForOrder1;
            }
            amountForOrder1 = trimToPrecision(amountForOrder1 - minAmountForOrder1, precisionForSymbol1);
        }
        return Optional.of(targetAmount);
    }

    private Double calculateMinAmount(Integer precision) {
        return Math.pow(10, -precision);
    }
}
