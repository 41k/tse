package root.tse.domain.chain_exchange_execution;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import root.tse.domain.ExchangeGateway;
import root.tse.domain.IdGenerator;
import root.tse.domain.order.Order;
import root.tse.domain.order.OrderType;

import java.time.Clock;
import java.util.Optional;

import static root.tse.domain.util.NumberUtil.trimToPrecision;

@Slf4j
@RequiredArgsConstructor
public class ChainExchangeService {

    private final IdGenerator idGenerator;
    private final ExchangeGateway exchangeGateway;
    private final InitialOrderAmountCalculator initialOrderAmountCalculator;
    private final ChainExchangeRepository chainExchangeRepository;
    private final Clock clock;

    public Optional<ChainExchange> tryToFormExpectedChainExchange(ChainExchangeExecutionContext context) {
        var symbols = context.getSymbols();
        var orderFeePercent = exchangeGateway.getOrderFeePercent();
        return exchangeGateway.getCurrentPrices(symbols)
            .flatMap(prices ->
                initialOrderAmountCalculator.tryToCalculate(context, prices, orderFeePercent)
                    .map(amountForOrder1 -> {
                        var orderExecutionType = context.getOrderExecutionType();
                        var symbol1 = symbols.get(0);
                        var priceForSymbol1 = prices.get(symbol1).get(OrderType.BUY);
                        var order1 = Order.builder()
                            .type(OrderType.BUY)
                            .executionType(orderExecutionType)
                            .symbol(symbol1)
                            .amount(amountForOrder1)
                            .price(priceForSymbol1)
                            .build();
                        var symbol2 = symbols.get(1);
                        var priceForSymbol2 = prices.get(symbol2).get(OrderType.SELL);
                        var amountForOrder2 = order1.getAmount();
                        var order2 = Order.builder()
                            .type(OrderType.SELL)
                            .executionType(orderExecutionType)
                            .symbol(symbol2)
                            .amount(amountForOrder2)
                            .price(priceForSymbol2)
                            .build();
                        var symbol3 = symbols.get(2);
                        var priceForSymbol3 = prices.get(symbol3).get(OrderType.SELL);
                        var amountForOrder3 = calculateAmountForOrder3(order2, orderFeePercent, context);
                        var order3 = Order.builder()
                            .type(OrderType.SELL)
                            .executionType(orderExecutionType)
                            .symbol(symbol3)
                            .amount(amountForOrder3)
                            .price(priceForSymbol3)
                            .build();
                        var profit = calculateProfit(order1, order3, orderFeePercent);
                        return ChainExchange.builder()
                            .id(idGenerator.generate())
                            .assetChainId(context.getAssetChainId())
                            .orderFeePercent(orderFeePercent)
                            .order1(order1)
                            .order2(order2)
                            .order3(order3)
                            .profit(profit)
                            .timestamp(clock.millis())
                            .build();
                    }));
    }

    public Optional<ChainExchange> tryToExecute(ChainExchange expectedChainExchange,
                                                ChainExchangeExecutionContext context) {
        var orderFeePercent = expectedChainExchange.getOrderFeePercent();
        var order1 = expectedChainExchange.getOrder1();
        return exchangeGateway.tryToExecute(order1)
            .flatMap(executedOrder1 -> {
                var amountForOrder2 = executedOrder1.getAmount();
                var order2 = expectedChainExchange.getOrder2().toBuilder().amount(amountForOrder2).build();
                return exchangeGateway.tryToExecute(order2)
                    .flatMap(executedOrder2 -> {
                        var amountForOrder3 = calculateAmountForOrder3(executedOrder2, orderFeePercent, context);
                        var order3 = expectedChainExchange.getOrder3().toBuilder().amount(amountForOrder3).build();
                        return exchangeGateway.tryToExecute(order3)
                            .map(executedOrder3 -> {
                                var profit = calculateProfit(executedOrder1, executedOrder3, orderFeePercent);
                                var executedChainExchange = expectedChainExchange.toBuilder()
                                    .order1(executedOrder1)
                                    .order2(executedOrder2)
                                    .order3(executedOrder3)
                                    .profit(profit)
                                    .build();
                                chainExchangeRepository.save(executedChainExchange);
                                return executedChainExchange;
                            });
                    });
            });
    }

    private Double calculateAmountForOrder3(Order order2, Double orderFeePercent, ChainExchangeExecutionContext context) {
        var symbol3 = context.getSymbols().get(2);
        var precisionForSymbol3 = context.getSymbolToPrecisionMap().get(symbol3);
        return trimToPrecision(order2.getNetTotal(orderFeePercent), precisionForSymbol3);
    }

    private Double calculateProfit(Order order1, Order order3, Double orderFeePercent) {
        return order3.getNetTotal(orderFeePercent) - order1.getNetTotal(orderFeePercent);
    }
}


// PROFIT CALCULATION EXAMPLE:
// (without precision trimming for simplicity)
//
// Chain: USD -> ETH -> BTC -> USD
//
// Symbols:
// symbol1 = ETH/USD
// symbol2 = ETH/BTC
// symbol3 = BTC/USD
//
// Prices:
// priceForSymbol1 = 2885.18 [USD]
// priceForSymbol2 = 0.06903 [BTC]
// priceForSymbol3 = 42500 [USD]
//
// exchangeAmount = 1000 [USD]
// orderFeePercent = 0.2% = 0.002
//
// amountForOrder1 = exchangeAmount / priceForSymbol1 = 1000 / 2885.18 = 0.3465988257231784 [ETH]
//
// Order1: BUY 0.3465988257231784 [ETH]
// order1NetTotal = (amountForOrder1 * priceForSymbol1) + (amountForOrder1 * priceForSymbol1 * orderFeePercent) =
// = (0.3465988257231784 * 2885.18) + (0.3465988257231784 * 2885.18 * 0.002) = 1002 [USD]
//
// Order2: SELL 0.3465988257231784 [ETH]
// amountForOrder3 = (amountForOrder1 * priceForSymbol2) - (amountForOrder1 * priceForSymbol2 * orderFeePercent) =
// = (0.3465988257231784 * 0.06903) - (0.3465988257231784 * 0.06903 * 0.002) = 0.0238778655057917 [BTC]
//
// Order3: SELL 0.0238778655057917 [BTC]
// order3NetTotal = (amountForOrder3 * priceForSymbol3) - (amountForOrder3 * priceForSymbol3 * orderFeePercent) =
// = (0.0238778655057917 * 42500) - (0.0238778655057917 * 42500 * 0.002) = 1,012.779665428155 [USD]
//
// PROFIT = order3NetTotal - order1NetTotal = 1,012.779665428155 - 1002 = 10.779665428155 [USD]


// -----------------


// If orderFeePercent = 0 then PROFIT can be calculated as:
//
// PROFIT = order3NetTotal - order1NetTotal =
//        = amountForOrder3 * priceForSymbol3 - amountForOrder1 * priceForSymbol1 =
//        = amountForOrder1 * priceForSymbol2 * priceForSymbol3 - amountForOrder1 * priceForSymbol1 =
//        = (exchangeAmount / priceForSymbol1) * priceForSymbol2 * priceForSymbol3 - (exchangeAmount / priceForSymbol1) * priceForSymbol1 =
//        = (exchangeAmount * priceForSymbol2 * priceForSymbol3) / priceForSymbol1 - exchangeAmount
//
// PROFIT = (exchangeAmount * priceForSymbol2 * priceForSymbol3) / priceForSymbol1 - exchangeAmount


// -----------------


// Dependency between PROFIT and difference between "FAIR" and "CURRENT" priceForSymbol2
//
// PROFIT is calculated with orderFeePercent = 0 for simplicity as
// PROFIT = (exchangeAmount * priceForSymbol2 * priceForSymbol3) / priceForSymbol1 - exchangeAmount
//
// fairPriceForSymbol2 = priceForSymbol1 / priceForSymbol3
//
// PROFIT --> fairPriceForSymbol2 - currentPriceForSymbol2
// or
// PROFIT --> (priceForSymbol1 / priceForSymbol3) - priceForSymbol2
//
//
// Calculations:
//
// Chain: USD -> ETH -> BTC -> USD
//
// Symbols:
// symbol1 = ETH/USD
// symbol2 = ETH/BTC
// symbol3 = BTC/USD
//
// priceForSymbol1=1986.25 / priceForSymbol2=0.06241454 / priceForSymbol3=33141.7 / profit=41.4217547227186
// 41.4217547227186 --> -0.0024824906482769
//
// priceForSymbol1=2074.7 / priceForSymbol2=0.06570786 / priceForSymbol3=32433.78 / profit=27.210813857811
// 27.210813857811  --> -0.0017406011729376
//
// priceForSymbol1=2885.18 / priceForSymbol2=0.06903 / priceForSymbol3=42500 / profit=16.8429699360178
// 16.8429699360178 --> -0.0011434117647059
//
// priceForSymbol1=1235 / priceForSymbol2=0.03193777 / priceForSymbol3=39068.28 / profit=10.3269157373278
// 10.3269157373278 --> -0.0003264474641729
//
// CONCLUSION: we have higher PROFIT when "CURRENT" priceForSymbol2 is significantly higher than "FAIR" priceForSymbol2
