package com.github.scs.impl;

import com.github.scs.api.WritableConfigurationSource;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Abstract implementation of a {@link WritableConfigurationSource} which is thread safe be utilising a {@link ReadWriteLock} allowing
 * for concurrent reads.
 *
 * @author Hendrik Louw
 * @since 2017-03-17.
 */
public abstract class AbstractThreadSafeConfigurationSource implements WritableConfigurationSource {

    /** Locks we use. */
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    public final void store(String key, String value) {
        Lock writeLock = readWriteLock.writeLock();
        try {
            writeLock.lock();
            storeInternal(key, value);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Extension point for storing configuration items in this source.
     * Called after this instance has obtained the {@link ReadWriteLock#writeLock() write lock}.
     * @param key The key of the configuration item being stored.
     * @param value The value of the configuration item being stored.
     */
    protected abstract void storeInternal(String key, String value);

    public final String retrieve(String key) {
        Lock readLock = readWriteLock.readLock();
        try {
            readLock.lock();
            return retrieveInternal(key);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Extension point for retrieving configuration items in this source.
     * Called after this instance has obtained the {@link ReadWriteLock#readLock() read lock}.
     * @param key The key of the configuration item which must be retrieved.
     * @return The value associated to the given key, or {@code null} if no entry defined.
     */
    protected abstract String retrieveInternal(String key);
}
