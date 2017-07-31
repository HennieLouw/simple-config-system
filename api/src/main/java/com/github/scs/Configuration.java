package com.github.scs;

import com.github.scs.api.ConfigurationDefaults;
import com.github.scs.api.ConfigurationSource;
import com.github.scs.api.PriorityOrderedSources;
import com.github.scs.impl.PrioritisedConfigurationSources;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

/**
 * Encapsulation of a configuration for an application with some defaults defined as well as providing a simple builder.
 * <p>
 * Note, that configuration values may be sourced from various {@link ConfigurationSource sources} which have been assigned a priority based on how the
 * configuration has been 'configured' when it was created.
 *
 * @author Hendrik Louw
 * @since 2017-03-17.
 */
@SuppressWarnings({"WeakerAccess", "SameParameterValue"})
public class Configuration implements PriorityOrderedSources {

    /** Logger we will use for the configuration. */
    private final Logger log;

    /** The name of this configuration. */
    @Getter
    private final String name;

    /** The defaults we will use. */
    @Getter
    private final ConfigurationDefaults defaults;

    /** The configuration sources we will use to support this configuration. */
    private final PrioritisedConfigurationSources sources;

    /** Internal UUID of the Configuration. */
    private final UUID internalId;

    /**
     * Creates a new Configuration instance with the given defaults and list of sources where the sources are prioritised based on their position in the given
     * list.
     * <p>
     * <b>Note:</b>
     * <p>
     * If the {@code defaults} given is {@code null}, this configuration will use the defaults as defined by {@link ConfigurationDefaults#buildCommon()}
     *
     * @param name The name of this configuration, if blank/null, it will default to the string representation of it's {@link #getUUID() UUID}.
     * @param defaults The defaults to use.
     * @param sources  The sources to retrieve and store the configuration items against.
     */
    @Builder
    public Configuration(String name, ConfigurationDefaults defaults, @Singular List<ConfigurationSource> sources) {
        this.internalId = UUID.randomUUID();
        if (StringUtils.isBlank(name)) {
            name = internalId.toString();
        }
        this.name = name;

        this.log = LoggerFactory.getLogger(String.format("com.github.scs.Configuration[%s]", name));

        if (defaults == null) {
            defaults = ConfigurationDefaults.buildCommon();
        }
        this.defaults = defaults;

        // Add the sources, prioritised as they are ordered in the given list.
        this.sources = new PrioritisedConfigurationSources();
        if (CollectionUtils.isNotEmpty(sources)) {
            int priority = 0;
            for (ConfigurationSource source : sources) {
                addSource(source, priority);
                priority++;
            }
        }
    }

    public UUID getUUID() {
        return internalId;
    }

    /**
     * Same as {@link #retrieveInteger(String, Integer)} with the default value set to {@link ConfigurationDefaults#getDefaultInteger()}
     *
     * @param key The key of the configuration value which must be returned.
     * @return The value of the given key, or the default value of {@link ConfigurationDefaults#getDefaultInteger()} if the configuration item is not found or
     * the value does not represent an integer.
     */
    public Integer retrieveInteger(String key) {
        return retrieveInteger(key, defaults.getDefaultInteger());
    }

    /**
     * Retrieves the integer value of the configuration item with the given key.
     *
     * @param key          The key of the configuration item which must be retrieved.
     * @param defaultValue The default value to return if the configuration item is not found or the value is not an integer.
     * @return The value of the given key, or the given default value if the configuration item is not found or the value does not represent an integer.
     */
    public Integer retrieveInteger(String key, Integer defaultValue) {
        Integer valueInt = defaultValue;
        try {
            valueInt = retrieveIntegerEx(key, defaultValue);
        } catch (NumberFormatException nFE) {
            String valueStr = retrieve(key);
            log.warn("Key Value Pair[{}]-[{}] caused NumberFormatException, returning default value of [{}]", key, valueStr, defaultValue);
        }
        return valueInt;
    }

    /**
     * Same as {@link #retrieveInteger(String, Integer)} but will throw a {@link NumberFormatException} if the value for the configuration items does not
     * represent a number.
     *
     * @param key          The key of the configuration item.
     * @param defaultValue The default value if the configuration item is not found.
     * @return The value of the configuration item, or the default value if the configuration item is not found.
     *
     * @throws NumberFormatException If the configuration item is found, but the string value does not represent an integer.
     */
    public Integer retrieveIntegerEx(String key, Integer defaultValue) throws NumberFormatException {
        String valueStr = retrieve(key);
        if (StringUtils.isBlank(valueStr)) {
            log.trace("Key Value Pair[{}]-[{}] is blank, returning default value of [{}]", key, valueStr, defaultValue);
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
    public void storeInteger(String key, Integer value) {
        if (value == null) {
            value = defaults.getDefaultInteger();
        }

        // Explicit use of ObjectUtils.toString to support Java 5 clients.
        // Can in the future be replaced with Objects.toString(String, String)
        @SuppressWarnings("deprecation")
        String valueStr = ObjectUtils.toString(value, null);
        store(key, valueStr);
    }

    /**
     * Same as {@link #retrieveBoolean(String, Boolean)}, but with the default value of {@link ConfigurationDefaults#getDefaultBoolean()}
     *
     * @param key The key of the boolean which must be retrieved.
     * @return The value of the configured key, or the default of {@link ConfigurationDefaults#getDefaultBoolean()}.
     */
    public Boolean retrieveBoolean(String key) {
        return retrieveBoolean(key, defaults.getDefaultBoolean());
    }

    /**
     * Retrieves the boolean value of the configuration, or the default value given if the key is not configured or the key's string value does not represent
     * a {@link ConfigurationDefaults#representsTrueBoolean(String) boolean text representation} as configured for this instance.
     *
     * @param key          The key of the boolean value which must be retrieved.
     * @param defaultValue The default value to return if the key is not configured or the value of the key is not a boolean value.
     * @return The value of the key being retrieved, or the default value.
     */
    public Boolean retrieveBoolean(String key, Boolean defaultValue) {
        // Boolean OR logic: if it's not a 'true' then we can only return 'true' if the default is 'true'
        String valueStr = retrieve(key);
        return defaults.representsTrueBoolean(valueStr) || defaultValue;
    }

    /**
     * Stores the given boolean configuration in all sources which support storing of configuration entries.
     * <p>
     * Note: If the {@code value == null}, then {@link ConfigurationDefaults#getDefaultBoolean()} will be used as the value which is stored.
     *
     * @param key   The key of the configuration which is being stored.
     * @param value The value of the configuration which is being stored
     */
    public void storeBoolean(String key, Boolean value) {
        if (value == null) {
            Boolean defaultBoolean = defaults.getDefaultBoolean();
            log.trace("Key Value Pair[{}]-[{}] has no value, defaulting to [{}]", defaultBoolean);
            value = defaultBoolean;
        }

        // Explicit use of ObjectUtils.toString to support Java 5 clients.
        // Can in the future be replaced with Objects.toString(String, String)
        @SuppressWarnings("deprecation")
        String valueStr = ObjectUtils.toString(value, null);
        storeInternal(key, valueStr);
    }

    /**
     * Same as {@link #retrieve(String, String)} with the {@link ConfigurationDefaults#getDefaultString()}  default } value.
     *
     * @param key The key of the configuration to retrieve.
     * @return The String value of the key retrieved.
     */
    public String retrieve(String key) {
        return retrieve(key, defaults.getDefaultString());
    }

    /**
     * Retrieves the String configuration, or the default value given if the key is not configured.
     *
     * @param key          The key of the configuration to retrieve.
     * @param defaultValue The default value if the key has not been defined in the configuration.
     * @return The value of the key, or the default value if the key has not been defined in the configuration.
     */
    public String retrieve(String key, String defaultValue) {
        String value = sources.retrieve(key);
        if (StringUtils.isBlank(value)) {
            log.trace("Value from source is null or blank, defaulting to value [{}]", defaultValue);
            value = defaultValue;
        }
        log.trace("Retrieved Key Value Pair[{}]-[{}].", key, value);
        return value;
    }

    /**
     * Stores the given key in the underlying configuration sources which supports saving.
     * <p>
     * Note: If {@code value} is {@link StringUtils#isBlank(CharSequence) blank}, then {@link ConfigurationDefaults#getDefaultString()} is stored instead.
     *
     * @param key   The key of the configuration to store.
     * @param value The value of the configuration to store.
     */
    public void store(String key, String value) {
        if (StringUtils.isBlank(value)) {
            String defaultString = defaults.getDefaultString();
            log.trace("Key Value Pair[{}]-[{}] has blank value, defaulting value[{}].", key, value, defaultString);

            value = defaultString;
        }
        storeInternal(key, value);
    }

    /**
     * Delegates the actual storage to the sources if the value is not null/blank.
     *
     * @param key   The key of the configuration item.
     * @param value The value of the configuration item.
     */
    private void storeInternal(String key, String value) {
        if (StringUtils.isNotBlank(value)) {
            sources.store(key, value);
            log.trace("Stored Key Value Pair[{}]-[{}] into underlying sources.", key, value);
        } else {
            log.warn("Store operation for key [{}] prevented as value is blank/null.");
        }
    }

    public void addSource(ConfigurationSource source, int priority) {
        sources.addSource(source, priority);
    }

    public void removeSource(ConfigurationSource source) {
        sources.removeSource(source);
    }

    public void removeAllSources() {
        sources.removeAllSources();
    }

}
