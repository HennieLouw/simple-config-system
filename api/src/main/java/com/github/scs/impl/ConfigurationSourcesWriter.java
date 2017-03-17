package com.github.scs.impl;

import com.github.scs.api.ConfigurationSource;
import com.github.scs.api.WritableConfigurationSource;

import java.util.Collection;

/**
 * Interface defining the writer which will be used when storing configuration keys to
 * the {@link WritableConfigurationSource} instances in a given collection.
 *
 * @author Hendrik Louw
 * @since 2017-03-15.
 */
public interface ConfigurationSourcesWriter {

    /**
     * Stores the given key, value configuration entry into the given sources which support updates.
     * @param key The key of the configuration entry.
     * @param value The value of the configuration entry.
     * @param sources The sources which they value must be stored into if supported.
     */
    void store(String key, String value, Collection<? extends ConfigurationSource> sources);

}
