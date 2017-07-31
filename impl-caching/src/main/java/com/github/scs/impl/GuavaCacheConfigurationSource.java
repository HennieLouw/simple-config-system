package com.github.scs.impl;

import com.github.scs.api.ConfigurationSource;
import com.github.scs.api.WritableConfigurationSource;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheBuilderSpec;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.AllArgsConstructor;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * A caching implementation of the {@link ConfigurationSource} which uses Google's Guava {@link LoadingCache} to
 * cache the result of the calls to some underlying configuration source.
 *
 * @author Hendrik Louw
 * @since 2017-07-31
 */
public class GuavaCacheConfigurationSource extends AbstractThreadSafeConfigurationSource {

    /** Default maximum number of items which will be cached, 25 items. */
    @SuppressWarnings("WeakerAccess")
    public static final long DEFAULT_MAX_SIZE = 25;

    /** Default time to live if none specified of 60 minutes. */
    @SuppressWarnings("WeakerAccess")
    public static final Long DEFAULT_TIME_TO_LIVE = 30L;

    /** Default time to idle if none specified of 10 minutes. */
    @SuppressWarnings("WeakerAccess")
    public static final Long DEFAULT_TIME_TO_IDLE = 10L;

    /** Default time unit if none specified. */
    @SuppressWarnings("WeakerAccess")
    public static final TimeUnit DEFAULT_TIME_UNIT = TimeUnit.MINUTES;

    /** The UUID we will utilise. */
    private final UUID uuid = UUID.randomUUID();

    /** The cache we will use. */
    private final LoadingCache<String, String> cache;

    /** The underlying source we will load from. */
    private final ConfigurationSource underlying;

    /**
     * Creates a new instance which will cache all values from the underlying configuration source with no limit
     * on the number of items being cache, nor the item for which the items will be cached.
     *
     * @param underlying The underlying configuration source to delegate
     */
    @SuppressWarnings({"WeakerAccess", "unused"})
    public GuavaCacheConfigurationSource(ConfigurationSource underlying) {
        this(underlying, null, null, null, null, null);
    }

    /**
     * Creates a new instance which will use the defaults defined for max size, time to live, time to idle
     *
     * @param underlying The underlying source to delegate writes (if supported) and cache misses to.
     */
    @SuppressWarnings({"WeakerAccess", "unused"})
    public GuavaCacheConfigurationSource(ConfigurationSource underlying, Object sizedConstructorMarker) {
        this(underlying,
             DEFAULT_MAX_SIZE,
             DEFAULT_TIME_TO_LIVE,
             DEFAULT_TIME_UNIT,
             DEFAULT_TIME_TO_IDLE,
             DEFAULT_TIME_UNIT);
    }

    /**
     * Create a new instance which will use the given values for the cache configuration.
     *
     * @param underlying      The underlying source to delegate writes (if supported) and cache misses to.
     * @param maxSize         The max size.
     * @param timeToLive      The time to live.
     * @param timeToLiveUnits The time unit of the time to live.
     * @param timeToIdle      The time to idle
     * @param timeToIdleUnits The time unit of the time to idle.
     */
    @SuppressWarnings({"WeakerAccess", "unused"})
    public GuavaCacheConfigurationSource(ConfigurationSource underlying,
                                         Long maxSize,
                                         Long timeToLive,
                                         TimeUnit timeToLiveUnits,
                                         Long timeToIdle,
                                         TimeUnit timeToIdleUnits) {
        this(underlying,
             ((CacheBuilderConfigurator) () -> {
                 CacheBuilder<Object, Object> builder = CacheBuilder.newBuilder();
                 if (maxSize != null) {
                     builder.maximumSize(maxSize);
                 }

                 if (timeToLive != null) {
                     TimeUnit timeUnit = timeToLiveUnits != null ? timeToLiveUnits : DEFAULT_TIME_UNIT;
                     builder.expireAfterWrite(timeToLive, timeUnit);
                 }

                 if (timeToIdle != null) {
                     TimeUnit timeUnit = timeToIdleUnits != null ? timeToIdleUnits : DEFAULT_TIME_UNIT;
                     builder.expireAfterAccess(timeToIdle, timeUnit);
                 }
                 return builder;
             }).configure());
    }

    /**
     * Creates a new instance with the given underlying source and cache configured as per the given builder spec
     * defined by {@link CacheBuilder#from(String)}
     *
     * @param underlying  The underlying source to delegate writes (if supported) and cache misses to.
     * @param builderSpec The cache builder spec which must be used.
     */
    public GuavaCacheConfigurationSource(ConfigurationSource underlying, String builderSpec) {
        this(underlying, CacheBuilder.from(builderSpec));
    }

    /**
     * Creates a new instance with the given underlying source and cache configured as per the given builder spec
     * defined by {@link CacheBuilder#from(CacheBuilderSpec)}
     *
     * @param underlying  The underlying source to delegate writes (if supported) and cache misses to.
     * @param builderSpec The cache builder spec which must be used.
     */
    public GuavaCacheConfigurationSource(ConfigurationSource underlying, CacheBuilderSpec builderSpec) {
        this(underlying, CacheBuilder.from(builderSpec));
    }

    /**
     * Most generic constructor which will create a new instance and delegate all writes and cache misses to the
     * given underlying configurations source which building the cache based on the state of the given cache builder.
     *
     * @param underlying   The underlying source to delegate writes (if supported) and cache misses to.
     * @param cacheBuilder The cache builder which has already been configured to the correct state for how the cache
     *                     must operate.
     */
    @SuppressWarnings({"WeakerAccess", "unused"})
    public GuavaCacheConfigurationSource(ConfigurationSource underlying, CacheBuilder<Object, Object> cacheBuilder) {
        this.underlying = underlying;
        GuavaCacheConfigurationSourceLoader loader = new GuavaCacheConfigurationSourceLoader(underlying);
        this.cache = cacheBuilder.build(loader);
    }

    public UUID getUUID() {
        return uuid;
    }

    @Override
    protected void storeInternal(String key, String value) {
        if (underlying instanceof WritableConfigurationSource) {
            ((WritableConfigurationSource) underlying).store(key, value);
            cache.invalidate(key);
        }
    }

    @Override
    protected String retrieveInternal(String key) {
        return cache.getUnchecked(key);
    }

    @AllArgsConstructor
    private static class GuavaCacheConfigurationSourceLoader extends CacheLoader<String, String> {

        /** The underlying source we will load from. */
        private final ConfigurationSource underlying;

        @SuppressWarnings("NullableProblems")
        public String load(String key) throws Exception {
            return underlying.retrieve(key);
        }
    }

    private interface CacheBuilderConfigurator {
        CacheBuilder<Object, Object> configure();
    }
}
