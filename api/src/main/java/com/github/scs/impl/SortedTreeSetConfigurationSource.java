package com.github.scs.impl;

import com.github.scs.api.ConfigurationSource;
import com.github.scs.api.PriorityOrderedSources;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A implementation of the sorted configuration sources which uses a Sorted Tree Set to perform the
 * sorting by wrapping the sources in a {@link SortedConfigurationSource} instance.
 *
 * @author Hendrik Louw
 * @since 2017-03-15.
 */
public class SortedTreeSetConfigurationSource implements PriorityOrderedSources {

    /**
     * The strategy which must be followed when configuration entries are being stored.
     *
     * @author Hennie Louw.
     */
    public enum WriteStrategy {
        /** All underlying sources which are writable must be delegated to. */
        ALL,

        /** Only the first highest priority writable source must be delegated to. */
        HIGHEST,

        /** Only the first lowest priority writable source must be delegated to. */
        LOWEST
    }

    /** The logger we will use. */
    private static final Logger LOG = LoggerFactory.getLogger(SortedTreeSetConfigurationSource.class);

    /** The tree set of configurations which are already sorted. */
    private SortedSet<SortedConfigurationSource> sortedConfigurations;

    /** The write strategy which must be used. */
    private WriteStrategy writeStrategy;

    /** The read/write locks which are used for concurrency. */
    private ReadWriteLock locks;

    /** Our UUID. */
    private final UUID internalId;

    /**
     * Creates an instance with the {@link WriteStrategy#ALL ALL} write strategy.
     */
    public SortedTreeSetConfigurationSource() {
        this(WriteStrategy.ALL);
    }

    /**
     * Creates an instance with the configured {@link WriteStrategy write stategy} for writing configurations and the given list of
     * sources
     *
     * @param writeStrategy The strategy to follow.
     * @param sources       Array of source to add to this instance, where their priority is defined by the position in the given array.
     */
    public SortedTreeSetConfigurationSource(WriteStrategy writeStrategy, ConfigurationSource... sources) {
        this(writeStrategy);
        for (int priority = 0; priority < sources.length; priority++) {
            addSource(sources[priority], priority);
        }
    }

    /**
     * Creates an instance with the configured {@link WriteStrategy write stategy} for writing configurations.
     *
     * @param writeStrategy The strategy to follow.
     */
    public SortedTreeSetConfigurationSource(WriteStrategy writeStrategy) {
        this.writeStrategy = writeStrategy;
        this.sortedConfigurations = new TreeSet<SortedConfigurationSource>();
        locks = new ReentrantReadWriteLock();
        internalId = UUID.randomUUID();
    }


    public UUID getUUID() {
        return internalId;
    }

    public String retrieve(String key) {
        Lock readLock = locks.readLock();
        try {
            readLock.lock();
            for (SortedConfigurationSource source : sortedConfigurations) {
                LOG.debug("Attempting key lookup from source [{}]", source);
                String value = source.retrieve(key);
                if (StringUtils.isNotBlank(value)) {
                    LOG.debug("Key found in source[{}], returning value of [{}]", source, value);
                    return value;
                }
            }
            LOG.debug("Key not found in any of the configured sources, returning null.");
            return null;
        } finally {
            readLock.unlock();
        }
    }

    public void addSource(ConfigurationSource source, int priority) {
        SortedConfigurationSource sortedConfig = new SortedConfigurationSource(source, priority);
        addSource(sortedConfig);
    }

    private void addSource(SortedConfigurationSource source) {
        Lock writeLock = locks.writeLock();
        try {
            writeLock.lock();
            LOG.debug("Adding ConfigurationSource[{}] to list with priority of [{}]", source, source.getPriority());
            sortedConfigurations.add(source);
        } finally {
            writeLock.unlock();
        }
    }

    public void removeSource(ConfigurationSource source) {
        Lock writeLock = locks.writeLock();
        try {
            writeLock.lock();
            LOG.debug("Searching configured sources for ConfigurationSource[{}] to be removed.", source);
            Iterator<SortedConfigurationSource> sortedConfigIterator = sortedConfigurations.iterator();
            while (sortedConfigIterator.hasNext()) {
                SortedConfigurationSource sortedConfig = sortedConfigIterator.next();
                // In rare cases, we may have our internal wrapper given for removal. In those cases we need to do both checks.
                if (sortedConfig.equals(source) || sortedConfig.underlyingEquals(source)) {
                    LOG.debug("Found instance of ConfigurationSource with priority of [{}] and removing.", sortedConfig.getPriority());
                    sortedConfigIterator.remove();
                    return;
                }
            }
        } finally {
            writeLock.unlock();
        }
    }

    public void removeAllSources() {
        Lock writeLock = locks.writeLock();
        try {
            writeLock.lock();
            sortedConfigurations.clear();
        } finally {
            writeLock.unlock();
        }
    }

    public void store(String key, String value) {
        Lock writeLock = locks.writeLock();
        try {
            writeLock.lock();
            if (writeStrategy == WriteStrategy.ALL) {
                storeAll(key, value);
            }

            if (writeStrategy == WriteStrategy.HIGHEST) {
                storeHighest(key, value);
            }

            if (writeStrategy == WriteStrategy.LOWEST) {
                storeLowest(key, value);
            }
        } finally {
            writeLock.unlock();
        }
    }

    private void storeAll(String key, String value) {
        for (SortedConfigurationSource sortedSource : sortedConfigurations) {
            if (sortedSource.isEncapsulatingWritable()) {
                sortedSource.store(key, value);
            }
        }
    }

    private void storeHighest(String key, String value) {
        for (SortedConfigurationSource sortedSource : sortedConfigurations) {
            if (sortedSource.isEncapsulatingWritable()) {
                sortedSource.store(key, value);
                break; // Stop after the first writable one has been found.
            }
        }
    }

    private void storeLowest(String key, String value) {
        // To store on the lowest, we need to invert the ordering.
        Comparator<Object> reverseOrder = Collections.reverseOrder();
        TreeSet<SortedConfigurationSource> reverseSet = new TreeSet<SortedConfigurationSource>(reverseOrder);
        reverseSet.addAll(this.sortedConfigurations);
        for (SortedConfigurationSource sortedSource : reverseSet) {
            if (sortedSource.isEncapsulatingWritable()) {
                sortedSource.store(key, value);
                break; // Stop after the first writable one has been found.
            }
        }
    }
}

