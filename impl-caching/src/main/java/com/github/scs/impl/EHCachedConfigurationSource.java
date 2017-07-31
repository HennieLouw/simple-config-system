package com.github.scs.impl;

import com.github.scs.api.ConfigurationSource;
import com.github.scs.api.WritableConfigurationSource;
import com.github.scs.util.EHCacheManagerResolutionStrategy;
import lombok.Getter;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.PersistenceConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * <p>An implementation of a configuration source which uses EHCache to cache the values retrieved from the underlying
 * configuration source.</p>
 * <p>
 * <p>The {@link CacheManager}, {@link CacheConfiguration configuration} and underlying configuration source must be
 * supplied at construction time. </p>
 *
 * @author Hendrik Louw
 * @since 2017-06-02
 */
public class EHCachedConfigurationSource extends AbstractThreadSafeConfigurationSource {

    /** Logger we will use. */
    private static final Logger LOG = LoggerFactory.getLogger(EHCachedConfigurationSource.class);

    /** The underlying source we will use. */
    @Getter
    private ConfigurationSource underlyingSource;

    /** The cache manager we are linked to. */
    @Getter
    private CacheManager cacheManager;

    /** The cache we pulled from our configuration. */
    @Getter
    private Cache cache;

    /** The uuid of this source. */
    private UUID uuid;

    public EHCachedConfigurationSource(ConfigurationSource underlyingSource,
                                       EHCacheManagerResolutionStrategy resolutionStrategy,
                                       String cacheName) {
        CacheConfiguration temporalConfig = new CacheConfiguration(cacheName, 0);
        initialise(underlyingSource, resolutionStrategy, temporalConfig, false);
    }

    public EHCachedConfigurationSource(ConfigurationSource underlyingSource,
                                       EHCacheManagerResolutionStrategy resolutionStrategy,
                                       int maxEntries,
                                       long timeToLiveSeconds,
                                       long timeToIdleSeconds) {
        CacheConfiguration configuration = configuration(maxEntries, timeToLiveSeconds, timeToIdleSeconds);
        initialise(underlyingSource, resolutionStrategy, configuration, true);
    }

    public EHCachedConfigurationSource(ConfigurationSource underlyingSource,
                                       EHCacheManagerResolutionStrategy resolutionStrategy,
                                       CacheConfiguration cacheConfiguration) {
        initialise(underlyingSource, resolutionStrategy, cacheConfiguration, true);
    }

    public UUID getUUID() {
        return uuid;
    }

    @Override
    protected void storeInternal(String key, String value) {
        if (underlyingSource instanceof WritableConfigurationSource) {
            ((WritableConfigurationSource) underlyingSource).store(key, value);
            cache.remove(key);
        }
    }

    @Override
    protected String retrieveInternal(String key) {
        Element cacheElement = cache.get(key);
        if (cacheElement == null) {
            String value = underlyingSource.retrieve(key);
            cacheElement = new Element(key, value);
            cache.put(cacheElement);
        }

        Object objectValue = cacheElement.getObjectValue();
        return String.valueOf(objectValue);
    }

    private void initialise(ConfigurationSource underlyingSource,
                            EHCacheManagerResolutionStrategy resolutionStrategy,
                            CacheConfiguration cacheConfiguration,
                            boolean registerCacheWithManager) {

        this.uuid = UUID.randomUUID();
        this.underlyingSource = underlyingSource;
        this.cacheManager = resolutionStrategy.resolveCacheManager();

        String cacheName = cacheConfiguration.getName();
        if (StringUtils.isBlank(cacheName)) {
            cacheName = String.valueOf(uuid);
            LOG.warn("Configuration did not specify name of Cache, using UUID of Configuration Source [{}]", cacheName);
            cacheConfiguration.setName(cacheName);
        }

        if (registerCacheWithManager) {
            this.cache = new Cache(cacheConfiguration);
            this.cacheManager.addCache(cache);
        } else {
            this.cache = this.cacheManager.getCache(cacheName);
        }
    }

    /**
     * Builds an EH Cache Configuration instance from the given parameters.
     *
     * @param maxEntries        The max number of entries in the cache.
     * @param timeToLiveSeconds The time to live in seconds.
     * @param timeToIdleSeconds The time to idle in seconds.
     * @return EH Cache configuration object which can be used to create a cache.
     */
    private CacheConfiguration configuration(int maxEntries, long timeToLiveSeconds, long timeToIdleSeconds) {
        CacheConfiguration cacheConfiguration = new CacheConfiguration(String.valueOf(uuid), maxEntries);
        cacheConfiguration.setMaxEntriesInCache(maxEntries);
        cacheConfiguration.setMaxEntriesLocalHeap(maxEntries);

        // Memory only instance.
        PersistenceConfiguration persistenceConfiguration = new PersistenceConfiguration();
        persistenceConfiguration.strategy(PersistenceConfiguration.Strategy.NONE);
        cacheConfiguration.persistence(persistenceConfiguration);
        cacheConfiguration.setOverflowToOffHeap(false);

        // We have Time to live and time to idle, so no external.
        cacheConfiguration.setEternal(false);
        cacheConfiguration.timeToLiveSeconds(timeToLiveSeconds);
        cacheConfiguration.timeToIdleSeconds(timeToIdleSeconds);
        return cacheConfiguration;
    }
}
