package com.github.scs.util;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.PersistenceConfiguration;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

/**
 * A Simple Facade over an EH Cache instance. As EH caches are thread safe, this implementation is as well.
 *
 * @author Hendrik Louw
 * @since 2017-04-08.
 */
@SuppressWarnings({"unchecked", "WeakerAccess", "unused"})
public class ExpiringMemoryCache<K, D> {

    /** Number of seconds in a minute. */
    public static final long MINUTE_SECONDS = 60;

    /** Number of seconds in an hour. */
    public static final long HOUR_SECONDS = MINUTE_SECONDS * 60;

    /** Default time to live if not supplied. */
    public static final long DEFAULT_TIME_TO_LIVE = MINUTE_SECONDS * 5;

    /** Default access timeout when not supplied. */
    public static final long DEFAULT_ACCESS_TIMEOUT = MINUTE_SECONDS * 5;

    /** Default size of the cache if not supplied. */
    public static final int DEFAULT_MAX_SIZE = 25;

    /** The EH cache we will use. */
    private final Cache cache;

    /** Identifier for the cache. */
    private final String cacheIdentifier;

    /**
     * Creates a new instance with the given identifier.
     * @param cacheIdentifier The id to identify this cache.
     */
    public ExpiringMemoryCache(String cacheIdentifier) {
        this(cacheIdentifier, DEFAULT_TIME_TO_LIVE, DEFAULT_ACCESS_TIMEOUT, DEFAULT_MAX_SIZE);
    }

    /**
     * Creates a new instance with the given configuration.
     *
     * Note.
     * All times in milliseconds.
     *
     * @param cacheIdentifier  The id ot identify this cache.
     * @param timeToLive       The time that each object in the cache should live for before it is evicted.
     * @param accessTimeOut    The time in milliseconds after which each object in the cache is cleared, if not accessed.
     * @param maximumCacheSize The maximum number of items to in the cache.
     */
    public ExpiringMemoryCache(String cacheIdentifier, long timeToLive, long accessTimeOut, int maximumCacheSize) {
        this.cacheIdentifier = String.format("ExpiringMemoryCache@%s(%s)", this.hashCode(), cacheIdentifier);
        CacheConfiguration cacheConfiguration = new CacheConfiguration(this.cacheIdentifier, maximumCacheSize);
        cacheConfiguration.setMaxEntriesLocalHeap(maximumCacheSize);

        cacheConfiguration.setTimeToIdleSeconds(accessTimeOut);
        cacheConfiguration.setTimeToLiveSeconds(timeToLive);
        cacheConfiguration.setEternal(false);
        cacheConfiguration.memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LFU);

        /* Do Not Allow this cache to off load to the disk. */
        PersistenceConfiguration persistenceConfiguration = new PersistenceConfiguration();
        persistenceConfiguration.setStrategy(PersistenceConfiguration.Strategy.NONE.name());
        cacheConfiguration.addPersistence(persistenceConfiguration);

        Configuration managerConfiguration = new Configuration();
        managerConfiguration.setName(this.cacheIdentifier+"-manager");
        managerConfiguration.setDynamicConfig(true);
        managerConfiguration.addCache(cacheConfiguration);
        CacheManager manager = CacheManager.newInstance(managerConfiguration);

        this.cache = manager.getCache(this.cacheIdentifier);
    }

    /**
     * Gets the size of the map.
     *
     * @return the size
     */
    public int size() {
        return cache.getSize();
    }

    /**
     * Check's if the cache is empty.
     *
     * @return {@code true} if the cache is empty, {@code false} otherwise.
     */
    public boolean isEmpty() {
        int size = cache.getSize();
        return size == 0;
    }

    /** Clears the cache, resetting the size to zero and nullifying references to avoid garbage collection issues. */
    public void clear() {
        cache.removeAll();
    }

    /** Runs the cleanup operation of the cache. */
    public void cleanup() {
        cache.evictExpiredElements();
    }

    /**
     * If the given key already maps to an existing object and the new object is not equal to the existing object, existing object is overwritten and the
     * existing object is returned; otherwise null is returned. You may want to check the return value for null to make sure you are not overwriting a
     * previously cached object.  May be you can use a different key for your object if you do not intend to overwrite.
     *
     * @param key         the key against which the object is associated
     * @param dataToCache the data to cache.
     */
    public void admit(K key, D dataToCache) {
        Element element = new Element(key, dataToCache);
        cache.put(element);
    }

    /**
     * Determines if the cache contains an entry for the given key.
     *
     * @param key The key for which to check.
     *
     * @return {@code true} if the cache contains a key which is recoverable, {@code false} otherwise.
     */
    public boolean contains(K key) {
        Element element = cache.getQuiet(key);
        return recoverable(element);
    }

    /**
     * Recovers the data associated with the given key from the cache if available and the data has not expired based on the access timeout and time to live
     * schematics of the cache.
     * @param key The key of the data which must be recovered.
     * @return The data associated with the given key, or {@code null} if the data was never cached or has expired.
     */
    public D recover(K key) {
        D recoverResult = null;
        Element element = cache.get(key);
        boolean recoverable = recoverable(element);
        if (recoverable) {
            recoverResult = (D) element.getObjectValue();
        }
        return recoverResult;
    }

    protected boolean recoverable(Element element) {
        return (element != null) && !element.isExpired();
    }

    public void remove(K key) {
        cache.remove(key);
    }


    public long getTimeToLive() {
        CacheConfiguration cacheConfiguration = cache.getCacheConfiguration();
        return cacheConfiguration.getTimeToLiveSeconds();
    }

    public long getAccessTimeOut() {
        CacheConfiguration cacheConfiguration = cache.getCacheConfiguration();
        return cacheConfiguration.getTimeToIdleSeconds();
    }

    public long getMaximumCacheSize() {
        CacheConfiguration configuration = cache.getCacheConfiguration();
        return configuration.getMaxEntriesInCache();
    }

    public String getCacheIdentifier() {
        return cacheIdentifier;
    }

    public int getSize() {
        return cache.getSize();
    }
}