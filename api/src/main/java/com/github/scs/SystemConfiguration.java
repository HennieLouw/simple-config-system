package com.github.scs;

import com.github.scs.api.ConfigurationSource;
import com.github.scs.api.PriorityOrderedSources;
import com.github.scs.impl.PrioritisedConfigurationSources;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Simple API for accessing configuration entries of a system.
 * <p>
 * Note, that configuration values may be sourced from various places which have been assigned a priority based on how the
 * configuration has been 'setup'.
 *
 * TODO Rework this as a registry of configurations with a Global instance for general system.
 * @author Hendrik Louw
 * @since 2017-03-15.
 */
@SuppressWarnings({"SameParameterValue", "JavaDoc"})
class SystemConfiguration {

    /** Default value if they key being retrieved is a string. */
    private static final String DEFAULT_STRING_VALUE = "";

    /** Default value if the key being retrieve is a number (int, long, etc). */
    private static final Integer DEFAULT_NUMERIC_VALUE = -1;

    /** Default value if the key being retrieved is a Boolean. */
    private static final Boolean DEFAULT_BOOLEAN_VALUE = Boolean.FALSE;

    /**
     * String representations of all values which will be considered a true value.
     * <ul>
     * <li>{@code "Y"}</li>
     * <li>{@code "YES"}</li>
     * <li>{@code "T"}</li>
     * <li>{@code "TRUE"}</li>
     * <li>{@code "1"}</li>
     * </ul>
     */
    private static final String[] BOOLEAN_TRUE_STRING_VALUES = new String[]{"Y", "YES", "T", "TRUE", "1"};

    /** The logger we will use. */
    private static final Logger LOG = LoggerFactory.getLogger(SystemConfiguration.class);

    /** The sorted sources we will use to have the ability to have a priority. */
    private static PriorityOrderedSources SORTED_SOURCES = new PrioritisedConfigurationSources();

    /**
     * Same as {@link #retrieveInteger(String, Integer)} with the default value set to {@link #DEFAULT_NUMERIC_VALUE}
     *
     * @param key The key of the configuration value which must be returned.
     * @return The value of the given key, or the default value of {@link #DEFAULT_NUMERIC_VALUE} if the configuration item is not found or the value does not
     * represent an integer.
     */
    public static Integer retrieveInteger(String key) {
        return retrieveInteger(key, DEFAULT_NUMERIC_VALUE);
    }

    /**
     * Retrieves the integer value of the configuration item with the given key.
     *
     * @param key          The key of the configuration item which must be retrieved.
     * @param defaultValue The default value to return if the configuration item is not found or the value is not an integer.
     * @return The value of the given key, or the given default value if the configuration item is not found or the value does not represent an integer.
     */
    private static Integer retrieveInteger(String key, Integer defaultValue) {
        Integer valueInt = defaultValue;
        try {
            valueInt = retrieveIntegerEx(key, defaultValue);
        } catch (NumberFormatException nFE) {
            String valueStr = retrieveString(key);
            String msg = String.format("Key[%s] - Value [%s] caused NumberFormatException, returning default value of [%s]", key, valueStr, defaultValue);
            LOG.warn(msg);
        }
        return valueInt;
    }

    /**
     * Same as {@link #retrieveIntegerEx(String, Integer)} but will throw a {@link NumberFormatException} if the value for the configuration items does not
     * represent a number.
     *
     * @param key          The key of the configuration item.
     * @param defaultValue The default value if the configuration item is not found.
     * @return The value of the configuration item, or the default value if the configuration item is not found.
     *
     * @throws NumberFormatException If the configuration item is found, but the string value does not represent an integer.
     */
    private static Integer retrieveIntegerEx(String key, Integer defaultValue) throws NumberFormatException {
        String valueStr = retrieveString(key);
        if (StringUtils.isBlank(valueStr)) {
            LOG.debug("Key[{}] - Value[{}] is blank, returning default value of [{}]", key, valueStr, defaultValue);
            return defaultValue;
        }
        return Integer.parseInt(valueStr);
    }

    /**
     * Stores the configuration item in the underlying sources which supports saving.
     *
     * @param key   The key of configuration item.
     * @param value The integer value of the configuration item or {@code #DEFAULT_NUMERIC_VALUE} if null.
     */
    public static void storeInteger(String key, Integer value) {
        if (value == null) {
            value = DEFAULT_NUMERIC_VALUE;
        }

        String valueStr = String.valueOf(value);
        storeString(key, valueStr);
    }

    /**
     * Same as {@link #retrieveBoolean(String, Boolean)}, but with the default value of {@link #DEFAULT_BOOLEAN_VALUE}
     *
     * @param key The key of the boolean which must be retrieved.
     * @return The value of the configured key, or the default of {@link #DEFAULT_BOOLEAN_VALUE}.
     */

    public static Boolean retrieveBoolean(String key) {
        return retrieveBoolean(key, DEFAULT_BOOLEAN_VALUE);
    }

    /**
     * Retrieves the boolean value of the configuration, or the default value given if the key is not configured
     * or the key's string value does not represent a {@link #BOOLEAN_TRUE_STRING_VALUES boolean text representation} as known by the SystemConfiguration.
     *
     * @param key          The key of the boolean value which must be retrieved.
     * @param defaultValue The default value to return if the key is not configured or the value of the key is not a boolean value.
     * @return The value of the key being retrieved, or the default value.
     */
    private static Boolean retrieveBoolean(String key, Boolean defaultValue) {
        // Retrieve the value as a string and then change it to a boolean.
        String valueStr = retrieveString(key);
        valueStr = valueStr.toUpperCase();

        boolean isTrueStringRepresentation = ArrayUtils.contains(BOOLEAN_TRUE_STRING_VALUES, valueStr);
        return isTrueStringRepresentation ? true : defaultValue;
    }

    /**
     * Stores the given boolean configuration in all sources which support storing of configuration entries.
     *
     * @param key   The key of the configuration which is being stored.
     * @param value The value of the configuration which is being stored, or {@link #DEFAULT_BOOLEAN_VALUE} if {@code null}
     */
    public static void storeBoolean(String key, Boolean value) {
        if (value == null) {
            value = DEFAULT_BOOLEAN_VALUE;
        }

        storeString(key, String.valueOf(value));
    }

    /**
     * Same as {@link #retrieveString(String, String)} with the {@link #DEFAULT_STRING_VALUE default } value.
     *
     * @param key The key of the configuration to retrieve.
     * @return The String value of the key retrieved.
     */

    private static String retrieveString(String key) {
        return retrieveString(key, DEFAULT_STRING_VALUE);
    }

    /**
     * Retrieves the String configuration, or the default value given if the key is not configured.
     *
     * @param key          The key of the configuration to retrieve.
     * @param defaultValue The default value if the key has not been defined in the configuration.
     * @return The value of the key, or the default value if the key has not been defined in the configuration.
     */
    private static String retrieveString(String key, String defaultValue) {
        LOG.trace("Retrieving value for key [{}] from configured sources", key);
        String value = SORTED_SOURCES.retrieve(key);
        if (StringUtils.isBlank(value)) {
            LOG.trace("Value from source is null or blank, defaulting to value [{}]", defaultValue);
            value = defaultValue;
        }
        return value;
    }

    /**
     * Stores the given key in the underlying configuration sources which supports saving.
     *
     * @param key   The key of the configuration to store.
     * @param value The value of the configuration to store.
     */
    private static void storeString(String key, String value) {
        SORTED_SOURCES.store(key, value);
    }

    /**
     * Registers the given {@link ConfigurationSource configuration source} with the system configuration
     * with the given priority.
     *
     * @param source   The source which must be registered.
     * @param priority The priority of the source, i.e. values for keys with a lower priority take precedence.
     */
    public static void addSource(ConfigurationSource source, int priority) {
        SORTED_SOURCES.addSource(source, priority);
    }

    /**
     * Removes the given {@link ConfigurationSource configuration source} from the collection of registered sources.
     *
     * @param source The source instance which must be removed.
     */
    public static void removeSource(ConfigurationSource source) {
        SORTED_SOURCES.removeSource(source);
    }

    /**
     * Removes all the sources from the system configuration.
     */
    public static void dropAllSources() {
        SORTED_SOURCES = new PrioritisedConfigurationSources();
    }
}
