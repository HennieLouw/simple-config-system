package com.github.scs.api;

import java.util.UUID;

/**
 * Primary interface for accessing some a configuration entry which has been stored in some
 * underlying storage.
 * <p>
 * This interface will assume the configuration values are represented as {@link String} instances.
 *
 * @author Hendrik Louw.
 * @since 2017-03-15.
 */
public interface ConfigurationSource {

    /**
     * Retrieve the {@link UUID Unique id} identifying this configuration source.
     * @return The unique id of this configuration source.
     */
    UUID getUUID();

    /**
     * Retrieves the value of the configuration key given from this underlying source.
     *
     * @param key The key of the configuration to be returned.
     * @return The value of the configuration with the given key or {@code null} if not defined in this source.
     */
    String retrieve(String key);
}
