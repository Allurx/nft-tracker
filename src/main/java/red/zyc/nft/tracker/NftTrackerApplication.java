package red.zyc.nft.tracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author zyc
 */
@EnableScheduling
@SpringBootApplication
public class NftTrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(NftTrackerApplication.class, args);
    }

}
