package Utkarsh.net.LeetCodeRevs.Services;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.stereotype.Service;

@Service
public class CacheInspectorService {

    @Autowired
    private CacheManager cacheManager;

    public void logCacheStats() {
        CaffeineCache cache = (CaffeineCache) cacheManager.getCache("leetcodeTitles");
        com.github.benmanes.caffeine.cache.Cache<Object, Object> nativeCache = cache.getNativeCache();
        System.out.println(nativeCache.stats());
    }


    @PostConstruct
    public void printCacheManager() {
        System.out.println("Cache Manager: " + cacheManager.getClass().getName());
    }
}