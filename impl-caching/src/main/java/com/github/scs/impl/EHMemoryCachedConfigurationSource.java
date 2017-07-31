package com.github.scs.impl;


import com.github.scs.api.ConfigurationSource;
import com.github.scs.util.EHCacheManagerResolutionStrategy;

/**
 * An extension of the {@link EHCachedConfigurationSource} which simplifies the creation of memory based
 * caches while using the {@link EHCacheManagerResolutionStrategy#SINGLETON} resolution strategy for resolving which
 * cache manager must be used.
 *
 * @author Hendrik Louw
 * @since 2017-04-08.
 */
public class EHMemoryCachedConfigurationSource extends EHCachedConfigurationSource {

    /** Default time to live in seconds of 30 minutes. */
    public static final long DEFAULT_TIME_TO_LIVE_SECONDS = 30 * 60;

    /** Default time to live in seconds of 10 minutes. */
    public static final long DEFAULT_TIME_TO_IDLE_SECONDS = 10 * 60;

    /** Default max entries of 25 configuration keys. */
    public static final int DEFAULT_MAX_ENTRIES = 25;

    public EHMemoryCachedConfigurationSource(ConfigurationSource underlyingSource) {
        this (underlyingSource, DEFAULT_MAX_ENTRIES);
    }

    public EHMemoryCachedConfigurationSource(ConfigurationSource underlyingSource, int maxEntries) {
        this (underlyingSource, maxEntries, DEFAULT_TIME_TO_LIVE_SECONDS, DEFAULT_TIME_TO_IDLE_SECONDS);
    }

    public EHMemoryCachedConfigurationSource(ConfigurationSource underlyingSource,
                                             int maxEntries,
                                             long timeToLiveSeconds,
                                             long timeToIdleSeconds) {
        super(underlyingSource,
              EHCacheManagerResolutionStrategy.SINGLETON,
              maxEntries,
              timeToLiveSeconds,
              timeToIdleSeconds);
    }
}
