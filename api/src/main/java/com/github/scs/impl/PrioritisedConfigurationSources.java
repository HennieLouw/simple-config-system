package com.github.scs.impl;

import com.github.scs.api.ConfigurationSource;
import com.github.scs.api.PriorityOrderedSources;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A implementation of the sorted configuration sources which uses a Tree Set to perform the
 * sorting by wrapping the sources in a {@link PrioritisedConfigurationSource} instance.
 *
 * @author Hendrik Louw
 * @since 2017-03-15.
 */
@SuppressWarnings({"WeakerAccess", "SameParameterValue"})
public class PrioritisedConfigurationSources implements PriorityOrderedSources {

    /** The logger we will use. */
    private static final Logger LOG = LoggerFactory.getLogger(PrioritisedConfigurationSources.class);

    /** The tree set of configurations which are already sorted. */
    private final SortedSet<PrioritisedConfigurationSource> sortedConfigurations;

    /** The write strategy which must be used. */
    private final ConfigurationSourcesWriteStrategy writeStrategy;

    /** The read/write locks which are used for concurrency. */
    private final ReadWriteLock locks;

    /** Our UUID. */
    private final UUID internalId;

    /** Creates an empty instance with the {@link ConfigurationSourcesWriteStrategy#ALL ALL} write strategy.  */
    public PrioritisedConfigurationSources() {
        this(ConfigurationSourcesWriteStrategy.ALL);
    }

    /**
     * Creates an instance with the {@link ConfigurationSourcesWriteStrategy#ALL ALL} write strategy and the given sources.
     * @param sources The sources which must be in this prioritised list.
     */
    public PrioritisedConfigurationSources(ConfigurationSource... sources) {
        this (ConfigurationSourcesWriteStrategy.ALL, sources);
    }

    /**
     * Creates an instance with the configured {@link ConfigurationSourcesWriteStrategy write stategy} for writing configurations.
     *
     * @param writeStrategy The strategy to follow.
     */
    public PrioritisedConfigurationSources(ConfigurationSourcesWriteStrategy writeStrategy) {
        this.writeStrategy = writeStrategy;
        this.sortedConfigurations = new TreeSet<PrioritisedConfigurationSource>();
        locks = new ReentrantReadWriteLock();
        internalId = UUID.randomUUID();
    }

    /**
     * Creates an instance with the configured {@link ConfigurationSourcesWriteStrategy write stategy} for writing configurations and the given list of
     * sources
     *
     * @param writeStrategy The strategy to follow.
     * @param sources       Array of source to add to this instance, where their priority is defined by the position in the given array.
     */
    public PrioritisedConfigurationSources(ConfigurationSourcesWriteStrategy writeStrategy, ConfigurationSource... sources) {
        this(writeStrategy);
        for (int priority = 0; priority < sources.length; priority++) {
            addSource(sources[priority], priority);
        }
    }


    public UUID getUUID() {
        return internalId;
    }

    public String retrieve(String key) {
        Lock readLock = locks.readLock();
        try {
            readLock.lock();
            for (PrioritisedConfigurationSource source : sortedConfigurations) {
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
        PrioritisedConfigurationSource sortedConfig = new PrioritisedConfigurationSource(source, priority);
        addSource(sortedConfig);
    }

    /**
     * Ads the given prioritised source to this set.
     * @param source The prioritised source to add.
     */
    private void addSource(PrioritisedConfigurationSource source) {
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
            Iterator<PrioritisedConfigurationSource> sortedConfigIterator = sortedConfigurations.iterator();
            while (sortedConfigIterator.hasNext()) {
                PrioritisedConfigurationSource sortedConfig = sortedConfigIterator.next();
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
            ConfigurationSourcesWriter sourcesWriter = writeStrategy.getWriter();
            sourcesWriter.store(key, value, sortedConfigurations);
        } finally {
            writeLock.unlock();
        }
    }
}

