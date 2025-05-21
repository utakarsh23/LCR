package Utkarsh.net.LeetCodeRevs.Config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.boot.context.event.ApplicationReadyEvent;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;

@Component //for preloading the Cache inside that text file in which we stored the cache for testing and saving even after restart
public class CachePreloader {

    @Autowired
    private CacheManager cacheManager;

    @EventListener(ApplicationReadyEvent.class)  //this @EventListener or @PostConstruct are lifecycle hooks—they let you execute code automatically at specific points in your application’s startup lifecycle.
                                                    //EventListener -> Runs when the whole Spring Boot app is fully ready to serve requests.
                                                    //PostConstruct -> Runs once, after the bean is created, but before the app is fully ready.
    public void preloadCacheFromFile() {
        Path path = Paths.get("cache_backup.txt");
        if (!Files.exists(path)) {
            System.out.println("No cache file found. Skipping preload.");
            return;
        }

        try {
            List<String> lines = Files.readAllLines(path);
            for (String line : lines) {
                String[] parts = line.split("=", 2);
                if (parts.length == 2) {
                    String titleSlug = parts[0].trim();
                    String link = parts[1].trim();

                    // Load into Caffeine cache manually
                    cacheManager.getCache("leetcodeLinks").put(titleSlug, link);
                }
            }
            System.out.println("Cache preloaded from file (" + lines.size() + " entries).");
        } catch (IOException e) {
            System.err.println("Failed to preload cache: " + e.getMessage());
        }
    }
}