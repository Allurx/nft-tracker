package red.zyc.nft.tracker.opensea;

import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import red.zyc.nft.tracker.opensea.model.Currency;
import red.zyc.nft.tracker.opensea.model.Nft;
import red.zyc.nft.tracker.opensea.model.Summary;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static red.zyc.toolkit.json.Json.JACKSON_OPERATOR;

/**
 * @author zyc
 */
@Slf4j
public class BabyDogeArmy {

    private static final RestTemplate REST_TEMPLATE = new RestTemplate();

    /**
     * <a href="https://binance-docs.github.io/apidocs/spot/cn/#ed913b7357">币安行情最新价格查询url</a>
     */
    private static final String BINANCE_LATEST_PRICE_URL = "https://api.binance.com/api/v3/ticker/price";

    /**
     * 非空正则
     */
    private static final Pattern NON_BLANK = Pattern.compile("\\S");

    /**
     * buy now元素定位
     */
    private static final By BUY_NOW_POSITION = By.xpath("//*[@id=\"Buy_Now\"]/div[2]/span/input");

    /**
     * 筛选后的数量元素定位
     */
    private static final By FILTERED_QUANTITY_POSITION = By.xpath("//*[@id=\"main\"]/div/div/div/div[5]/div/div[3]/div[3]/div[3]/div[2]/div/div/p");

    /**
     * 最小视图元素定位
     */
    private static final By MIN_VIEW_POSITION = By.xpath("//*[@id=\"main\"]/div/div/div/div[5]/div/div[3]/div[1]/div/div/div/div/div[4]/div/div/button[2]");

    /**
     * nft元素定位
     */
    private static final By NFT_POSITION = By.cssSelector("div[role='gridcell']");

    /**
     * nft元素id定位
     */
    private static final By NFT_ID_POSITION = By.xpath(".//div/article/a/div[2]/div[1]/div/div");

    /**
     * nft元素价格货币定位
     */
    private static final By NFT_PRICE_CURRENCY_POSITION = By.xpath(".//div/article/a/div[2]/div[2]/div/div/div[2]/div[1]/div/img");

    /**
     * nft元素价格定位
     */
    private static final By NFT_PRICE_POSITION = By.xpath(".//div/article/a/div[2]/div[2]/div/div/div[2]/div[2]");

    /**
     * 地板价元素定位
     */
    private static final By FLOOR_PRICE = By.xpath("//*[@id=\"main\"]/div/div/div/div[5]/div/div[1]/div/div[2]/div[3]/div/div[4]/a/div/span[1]/div");

    public static void main(String[] args) throws InterruptedException {

        // 让java程序走代理服务器
        System.setProperty("proxyHost", "127.0.0.1");
        System.setProperty("proxyPort", "7890");

        WebDriver webDriver = WebDriverManager.chromedriver().create();
        FluentWait<WebDriver> waiter = new WebDriverWait(webDriver, Duration.ofSeconds(10), Duration.ofSeconds(1));
        Actions action = new Actions(webDriver);
        webDriver.manage().window().maximize();
        //webDriver.get("https://opensea.io/collection/babydogearmy");
        webDriver.get("https://opensea.io/collection/boredapeyachtclub");

        // 只显示当前可购买的nft
        waiter.until(ExpectedConditions.presenceOfElementLocated(BUY_NOW_POSITION)).click();

        // 显示nft最小视图
        waiter.until(ExpectedConditions.presenceOfElementLocated(MIN_VIEW_POSITION)).click();

        // 筛选后的数量
        waiter.until(ExpectedConditions.textMatches(FILTERED_QUANTITY_POSITION, NON_BLANK));
        int filteredQuantity = Integer.parseInt(webDriver.findElement(FILTERED_QUANTITY_POSITION).getText().replaceAll("[^0-9]", ""));

        // 地板价
        waiter.until(ExpectedConditions.textMatches(FLOOR_PRICE, NON_BLANK));
        BigDecimal floorPrice = new BigDecimal(webDriver.findElement(FLOOR_PRICE).getText().replaceAll("[^.0-9]", ""));

        Map<String, Nft> nfts = new HashMap<>(1024);
        TreeMap<Integer, LocalDateTime> limiter = new TreeMap<>();

        while (nfts.size() < filteredQuantity) {

            action.sendKeys(Keys.PAGE_DOWN).perform();

            // 由于网络问题可能数据加载不全
            Thread.sleep(1200);

            // todo 由于网络问题会导致NFT_POSITION加载不全
            webDriver.findElements(NFT_POSITION).forEach(element -> {
                String text = null;
                try {

                    text = element.getText();

                    // nft id
                    waiter.until(ExpectedConditions.and(ExpectedConditions.presenceOfNestedElementLocatedBy(element, NFT_ID_POSITION), ExpectedConditions.textMatches(NFT_ID_POSITION, NON_BLANK)));
                    String id = element.findElement(NFT_ID_POSITION).getText();

                    // nft价格货币
                    WebElement priceCurrencyElement;
                    waiter.until(ExpectedConditions.and(ExpectedConditions.presenceOfNestedElementLocatedBy(element, NFT_PRICE_CURRENCY_POSITION), ExpectedConditions.attributeToBeNotEmpty((priceCurrencyElement = element.findElement(NFT_PRICE_CURRENCY_POSITION)), "alt")));

                    // nft价格
                    waiter.until(ExpectedConditions.and(ExpectedConditions.presenceOfNestedElementLocatedBy(element, NFT_PRICE_POSITION), ExpectedConditions.textMatches(NFT_PRICE_POSITION, NON_BLANK)));

                    // 组装nft对象
                    Nft nft = Nft.builder()
                            .id(id)
                            .currency(priceCurrencyElement.getAttribute("alt"))
                            .price(new BigDecimal(element.findElement(NFT_PRICE_POSITION).getText().replaceAll("[^.0-9]", "")))
                            .build();

                    // 避免重复的nft
                    nfts.putIfAbsent(id, nft);

                } catch (StaleElementReferenceException e) {
                    log.error("元素[{}]已从dom中移除", text, e);
                } catch (NoSuchElementException e) {
                    log.error("元素[{}]定位失败", text, e);
                } catch (Exception e) {
                    log.error("发生其它异常，当前元素[{}]信息为：{}", text, e);
                }

            });

            // 超时限制器
            limiter.putIfAbsent(nfts.size(), LocalDateTime.now());

            // 网络异常或者出现bug避免死循环
            if (limiter.get(limiter.lastKey()).until(LocalDateTime.now(), ChronoUnit.SECONDS) >= 20) {
                log.info("轮询完毕或出现异常，轮询结果为：{}", JACKSON_OPERATOR.toJsonString(nfts.values()));
                break;
            }
        }

        // 当前采集到的nft使用的所有货币
        Set<String> currencyNames = nfts.values().stream().collect(Collectors.groupingBy(Nft::getCurrency)).keySet();
        final List<Currency> currencies = buildCurrency(currencyNames);

        // 本次采集的汇总信息
        Summary<Nft> summary = Summary.<Nft>builder().filteredQuantity(filteredQuantity).floorPrice(floorPrice).nfts(nfts.values()).currencies(currencies).build();

        log.info("采集到的nft信息为:{}", JACKSON_OPERATOR.toJsonString(summary));


        //BigDecimal totalPrice = summary.getNfts().stream().filter(nft -> nft.getPrice().compareTo(BigDecimal.ONE) <= 0).reduce(BigDecimal.ZERO, (b, nft) -> b.add(nft.getPrice()), (bigDecimal, bigDecimal2) -> null);
        //log.info("单价低于1eth的nft总价为:{}", totalPrice);

    }

    /**
     * 调用币安接口查询当前货币价格
     *
     * @param currencyNames 货币名称
     * @return 货币信息
     */
    private static List<Currency> buildCurrency(Set<String> currencyNames) {

        // 根据货币名称获取交易对名称
        String[] symbols = currencyNames.stream().map(Currency::convertNameToSymbol).distinct().toArray(String[]::new);

        // 请求参数封装
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("symbols", StringUtils.deleteWhitespace(JACKSON_OPERATOR.toJsonString(symbols)));

        // 构造请求
        URI uri = UriComponentsBuilder.fromUriString(BINANCE_LATEST_PRICE_URL)
                .queryParams(map)
                .encode().build().toUri();
        RequestEntity<Void> request = RequestEntity.method(HttpMethod.GET, uri).build();
        List<Currency> currencies = Optional.ofNullable(REST_TEMPLATE.exchange(request, new ParameterizedTypeReference<List<Currency>>() {
        }).getBody()).orElseThrow(() -> new RuntimeException("调用币安接口获取交易对价格失败"));

        // 获取交易对的货币名称
        currencies.forEach(currency -> currency.setName(Currency.convertSymbolToName(currency.getSymbol())));
        return currencies;
    }
}
