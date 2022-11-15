package red.zyc.nft.tracker.opensea.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * @author zyc
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Nft {

    /**
     * nft id
     */
    private String id;

    /**
     * 当前价格使用的货币
     */
    private String currency;

    /**
     * 当前价格
     */
    private BigDecimal price;

}
