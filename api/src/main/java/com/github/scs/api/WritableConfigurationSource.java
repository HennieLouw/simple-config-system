package com.github.scs.api;

/**
 * Natural extension of a {@link ConfigurationSource} which additionally provides the ability of writing a configuration
 * entry to the underlying storage.
 * <p>
 * Note, this interface provides no contract for concurrency.
 *
 * @author Hendrik Louw.
 * @since 2017-03-15.
 */
public interface WritableConfigurationSource extends ConfigurationSource {

    /**
     * Stores the given key value pair in this underlying configuration source.
     *
     * @param key   The key of the configuration which must be stored.
     * @param value The value of the configuration which must be stored.
     */
    void store(String key, String value);

}
