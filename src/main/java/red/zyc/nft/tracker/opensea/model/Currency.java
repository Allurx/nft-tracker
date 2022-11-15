package red.zyc.nft.tracker.opensea.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zyc
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Currency {

    /**
     * 货币名称
     */
    private String name;

    /**
     * 转换成的货币交易对名称
     */
    private String symbol;

    /**
     * 当前价格
     */
    private BigDecimal price;

    /**
     * 根据货币名称转换相应的交易对名称以便查询最新价格
     *
     * @param name 货币名称
     * @return 用来查询最新价格的交易对名称
     */
    public static String convertNameToSymbol(String name) {
        return CURRENCY_TRADE_PAIR.computeIfAbsent(name, k -> k + "USDT");
    }

    /**
     * 根据交易对名称转换相应的货币名称
     *
     * @param symbol 交易对名称
     * @return 相应的货币名称
     */
    public static String convertSymbolToName(String symbol) {
        return CURRENCY_TRADE_PAIR.entrySet().stream().filter(entry -> entry.getValue().equals(symbol)).findFirst().orElseThrow(RuntimeException::new).getKey();
    }

    /**
     * 所有货币和对应的交易对，默认情况下交易对 = 货币名称 + "USDT"
     */
    public static final Map<String, String> CURRENCY_TRADE_PAIR = new HashMap<>();

    // 一些特殊的货币与其对应的交易对
    static {
        CURRENCY_TRADE_PAIR.put("WETH", "ETHUSDT");
        CURRENCY_TRADE_PAIR.put("WBTC", "WBTCBUSD");
    }

}
