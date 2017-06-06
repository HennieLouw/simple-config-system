package com.github.scs.util;

import com.github.scs.impl.EHCachedConfigurationSource;
import net.sf.ehcache.CacheManager;

/**
 * An interface providing the contract which will be followed by a
 * {@link EHCachedConfigurationSource} when determining which {@link CacheManager} must be
 * used to create new instances.
 *
 * @author Hendrik Louw
 * @since 2017-06-02
 */
public interface CacheManagerResolutionStrategy {

    /** Strategy which provides a new cache manager for each call to {@link #resolveCacheManager()} */
    CacheManagerResolutionStrategy NEW_EMPTY_MANAGER = new EmptyCacheManagerStrategy();

    /** Strategy which provides a single instance for all calls to {@link #resolveCacheManager()} */
    CacheManagerResolutionStrategy SINGLETON = new SingletonCacheManagerStrategy();

    /**
     * Resolves the cache manager which must be used when a {@link EHCachedConfigurationSource}
     * is created for a given configuration.
     *
     * @return The cache manager which will be used to manage the cache.
     */
    CacheManager resolveCacheManager();

    /**
     * Implementation which will always provide a new Cache Manager when {@link #resolveCacheManager()} is called
     * by invoking {@link CacheManager#create()};
     * <p>
     * <p/><b>Please Note:</b>
     * <p> This implementation should only be used if correct shutdown of the cache manager is not necessary. </br>
     * See <a href="http://www.ehcache.org/documentation/2.8/operations/shutdown.html">Shutting Down Ehcache</a>
     * </p>
     *
     * @author Hendrik Louw.
     * @since 2017-06-02
     */
    class EmptyCacheManagerStrategy implements CacheManagerResolutionStrategy {

        private EmptyCacheManagerStrategy() {
        }

        public CacheManager resolveCacheManager() {
            return CacheManager.create();
        }

        public static void main(String[] args) {
            CacheManager manager = CacheManager.create();
        }
    }

    /**
     * Implementation which will always return the same instance, but whose instance is not managed in terms of shutdown
     * as it is intended to be used with non persisted caches.
     * <p>
     * <p/><b>Please Note:</b>
     * <p> This implementation should only be used if correct shutdown of the cache manager is not necessary. </br>
     * See <a href="http://www.ehcache.org/documentation/2.8/operations/shutdown.html">Shutting Down Ehcache</a>
     * </p>
     *
     * @author Hendrik Louw
     * @since 2017-06-02
     */
    class SingletonCacheManagerStrategy implements CacheManagerResolutionStrategy {

        /** The cache manager we will be using. */
        private final CacheManager cacheManager;

        private SingletonCacheManagerStrategy() {
            cacheManager = CacheManager.create();
        }

        public CacheManager resolveCacheManager() {
            return cacheManager;
        }
    }


}
