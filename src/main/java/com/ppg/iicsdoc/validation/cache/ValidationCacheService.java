package com.ppg.iicsdoc.validation.cache;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.ppg.iicsdoc.model.validation.SchemaValidationResult;
import com.ppg.iicsdoc.util.FileHasher;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ValidationCacheService {
    
    private final ConcurrentHashMap<String, CachedValidationResult> cache = new ConcurrentHashMap<>();
    private final Duration cacheTTL = Duration.ofHours(24);

    @Data
    @AllArgsConstructor
    private static class CachedValidationResult {
        private String fileHash;
        private SchemaValidationResult result;
        private LocalDateTime cachedAt;
        private long fileSize;
    }

    public SchemaValidationResult getCached(Path xmlFile) {
        try {
            String filePath = xmlFile.toAbsolutePath().toString();
            CachedValidationResult cached = cache.get(filePath);

            if (cached == null) {
                return null;
            }

            if (Duration.between(cached.getCachedAt(), LocalDateTime.now()).compareTo(cacheTTL) > 0) {
                log.debug("Cache expire for: {}", xmlFile.getFileName());

                cache.remove(filePath);
                return null;
            }

            String currentHash = FileHasher.calculateFileHash(xmlFile);
            long currentSize = Files.size(xmlFile);

            if (!currentHash.equals(cached.getFileHash()) || currentSize != cached.getFileSize()) {
                log.debug("File changed, cache invalidated: {}", xmlFile.getFileName());

                cache.remove(filePath);
                return null;
            }

            log.info("Cache hit for: {}", xmlFile.getFileName());
            return cached.getResult();
        } catch (Exception e) {
            log.warn("Error checking cache", e);
            return null;
        }
    }

    public void cache(Path xmlFile, SchemaValidationResult result) {
        try {
            String filePath = xmlFile.toAbsolutePath().toString();
            String fileHash = FileHasher.calculateFileHash(xmlFile);
            long fileSize = Files.size(xmlFile);

            CachedValidationResult cached = new CachedValidationResult(
                fileHash,
                result,
                LocalDateTime.now(),
                fileSize);
            
            cache.put(filePath, cached);
            log.debug("Cached validation result for: {}", xmlFile.getFileName());
        } catch (Exception e) {
            log.warn("Error caching result", e);
        }
    }

    public void clear() {
        cache.clear();
        log.info("Validation cache cleared");
    }

    public void clearExpired() {
        LocalDateTime now = LocalDateTime.now();
        cache.entrySet().removeIf(entry -> Duration.between(
            entry.getValue().getCachedAt(), 
            now).compareTo(cacheTTL) > 0);

        log.info("Cleared expired cache entries");
    }

    public CacheStats getStats() {
        return new CacheStats(
            cache.size(),
            cache.values().stream()
                .mapToLong(c -> c.getFileSize())
                .sum());
    }

    @Data
    @AllArgsConstructor
    public static class CacheStats {
        private int cachedFiles;
        private long totalCachedBytes;
    }
}
