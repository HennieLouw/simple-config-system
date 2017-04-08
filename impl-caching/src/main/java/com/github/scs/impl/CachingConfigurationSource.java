package com.github.scs.impl;


import com.github.scs.api.ConfigurationSource;
import com.github.scs.api.WritableConfigurationSource;
import com.github.scs.util.ExpiringMemoryCache;
import org.apache.commons.lang3.StringUtils;

import java.util.UUID;

/**
 * Implementation of a configuration source which will cache the results from the operation {@link ConfigurationSource#retrieve(String) retrieve} and drop the
 * cached entry when a {@link WritableConfigurationSource#store(String, String)} is called.
 * <p>
 * Note, the caching functionality is provided via EH Cache.
 *
 * @author Hendrik Louw
 * @since 2017-04-08.
 */
public class CachingConfigurationSource implements WritableConfigurationSource {

    /** The underlying source we are caching entries for. */
    private final ConfigurationSource underlyingSource;

    /** The cache we will use. */
    private final ExpiringMemoryCache<String, String> cache;

    /** The UUID of this configuration source. */
    private final UUID uuid;

    /**
     * Creates a new instance of this caching configuration source which will cache entries for the given underlying source.
     *
     * @param underlyingSource The underlying source we will cache entries for.
     */
    public CachingConfigurationSource(ConfigurationSource underlyingSource) {
        this(underlyingSource, new ExpiringMemoryCache<String, String>(UUID.randomUUID().toString()), true);

    }

    /**
     * Create a new instance of this caching configuration source which will cache entries for the given underlying source into the given
     * cache instance.
     *
     * @param underlyingSource The underlying source we will cache entries for.
     * @param cache            The cache we will use to cache the entries.
     */
    public CachingConfigurationSource(ConfigurationSource underlyingSource, ExpiringMemoryCache<String, String> cache) {
        this(underlyingSource, cache, false);
    }

    /**
     * Private constructor supporting the two public constructors.
     *
     * @param underlyingSource The underlying source.
     * @param cache            The cache.
     * @param uuidFromCache    if the source UUID must be generated from the cache ID or generated.
     */
    private CachingConfigurationSource(ConfigurationSource underlyingSource, ExpiringMemoryCache<String, String> cache, boolean uuidFromCache) {
        if (uuidFromCache) {
            this.uuid = UUID.fromString(cache.getCacheIdentifier());
        } else {
            this.uuid = UUID.randomUUID();
        }

        this.underlyingSource = underlyingSource;
        this.cache = cache;
    }

    public UUID getUUID() {
        return uuid;
    }

    public void store(String key, String value) {
        // On store, we need to delete the cache entry.
        if (underlyingSource instanceof WritableConfigurationSource) {
            WritableConfigurationSource writableSource = (WritableConfigurationSource) underlyingSource;
            writableSource.store(key, value);
            cache.remove(key);
        }
    }

    public String retrieve(String key) {
        String cachedValue = cache.recover(key);
        if (StringUtils.isBlank(cachedValue)) {
            cachedValue = underlyingSource.retrieve(key);
            cache.admit(key, cachedValue);
        }
        return cachedValue;
    }
}
