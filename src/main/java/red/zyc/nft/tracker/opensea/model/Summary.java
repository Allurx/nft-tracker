package red.zyc.nft.tracker.opensea.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

/**
 * @param <T> nft类型
 * @author zyc
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Summary<T> {

    /**
     * 按条件过滤后的数量
     */
    private Integer filteredQuantity;

    /**
     * 地板价
     */
    private BigDecimal floorPrice;

    /**
     * 实际采集到的nft集合
     */
    private Collection<T> nfts;

    /**
     * 当前采集到的nft使用的所有货币
     */
    private List<Currency> currencies;

}
