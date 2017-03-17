package com.github.scs.impl;

import com.github.scs.api.PriorityOrderedSources;

/**
 * Encapsulation of possible strategies which can be used when a {@link PriorityOrderedSources#store(String, String)} in called.
 *
 * @author Hennie Louw.
 * @since 2017-03-17
 */
public enum ConfigurationSourcesWriteStrategy {
    /** All underlying sources which are writable must be delegated to. */
    ALL (SimpleSourcesWriter.INSTANCE),

    /** Only the first highest priority writable source must be delegated to. */
    HIGHEST (PrioritisedSourcesWriter.HIGHEST),

    /** Only the first lowest priority writable source must be delegated to. */
    LOWEST (PrioritisedSourcesWriter.LOWEST);

    /** The writer which will be used for this strategy. */
    private final ConfigurationSourcesWriter writer;

    ConfigurationSourcesWriteStrategy(ConfigurationSourcesWriter writer) {
        this.writer = writer;
    }

    public ConfigurationSourcesWriter getWriter() {
        return writer;
    }
}
