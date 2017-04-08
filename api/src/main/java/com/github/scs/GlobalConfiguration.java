package com.github.scs;

import com.github.scs.api.ConfigurationDefaults;
import com.github.scs.api.ConfigurationSource;
import com.github.scs.impl.MemoryConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Utility class which provides a 'global' configuration object which allows for easy access within an application without the need to
 * pass a specific {@link Configuration} instance around.
 * <p>
 * The class also allows you to configure the global configuration source with your list of sources.
 *
 * @author Hendrik Louw
 * @since 2017-03-27.
 */
public class GlobalConfiguration {

    /** Name of the global configuration. */
    public static final String CONFIGURATION_NAME = "GLOBAL";
    /** Global Configuration instance. */
    private static Configuration GLOBAL_CONFIG;

    public static Configuration instance() {
        if (GLOBAL_CONFIG == null) {
            GLOBAL_CONFIG = buildGlobal();
        }
        return GLOBAL_CONFIG;
    }

    public static Configuration configure(ConfigurationDefaults defaults, ConfigurationSource... sources) {
        List<ConfigurationSource> sourceList = Arrays.asList(sources);
        GLOBAL_CONFIG = buildGlobal(defaults, sourceList);
        return instance();
    }

    private static Configuration buildGlobal() {
        // We must provide both the defaults and the sources.
        ConfigurationDefaults defaults = ConfigurationDefaults.buildCommon();
        return buildGlobal(defaults);
    }

    private static Configuration buildGlobal(List<ConfigurationSource> sources) {
        // We need to provide the defaults.
        ConfigurationDefaults defaults = ConfigurationDefaults.buildCommon();
        return buildGlobal(defaults, sources);
    }

    private static Configuration buildGlobal(ConfigurationDefaults defaults) {
        // We only providing the defaults, so use the memory source as our source.
        ConfigurationSource memorySource = new MemoryConfigurationSource();
        List<ConfigurationSource> sources = Arrays.asList(memorySource);
        return buildGlobal(defaults, sources);
    }

    private static Configuration buildGlobal(ConfigurationDefaults defaults, List<ConfigurationSource> sources) {
        return new Configuration(CONFIGURATION_NAME, defaults, sources);
    }

}
