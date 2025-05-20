package Utkarsh.net.LeetCodeRevs.Config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

//for managing the caches, we are using caffein, the CacheManager for springboot, Redis and SB inbuilt cache manager can also be used
@Configuration
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("leetcodeTitles", "leetcodeLinks", "leetcodeTotalSubs"); //the titles for which we are managing teh cache,(check the dailyUpdateQuestionAndWeightService for reference)
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofHours(8)) //expiring time for the caches
                .maximumSize(500) //500 tags can be cached(it's not size, it's no of tags)(no of  caches managed)
                .recordStats());
        return cacheManager;
    }
}