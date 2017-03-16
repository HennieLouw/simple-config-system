package com.github.scs.impl;


import com.github.scs.api.ConfigurationSource;
import com.github.scs.api.WritableConfigurationSource;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.UUID;

/**
 * An implementation of a ConfigurationSource which does not actually implement the logic of retrieving the configuration
 * entry, but instead adds an additional meta data information that the underlying source it delegates to has some priority.
 * <p>
 * Note, the priority is encapsulated in a numerical value where lower numbers are more important than higher numbers, i.e. natural sorting of the priority.
 *
 * @author Hendrik Louw
 * @since 2017-03-15.
 */
public class PrioritisedConfigurationSource implements Comparable<PrioritisedConfigurationSource>, ConfigurationSource, WritableConfigurationSource {

    /** The priority value for the source, lower numbers are considered more important than higher. */
    private int priority;

    /** The source which this implementation is adding the priority meta data to. */
    private ConfigurationSource underlyingSource;

    /**
     * Creates a new instance which will delegate to the underlying source.
     *
     * @param source   The source to delegate {@link #retrieve(String)} to.
     * @param priority The priority of the delegating source.
     */
    public PrioritisedConfigurationSource(ConfigurationSource source, int priority) {
        this.underlyingSource = source;
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }

    public UUID getUUID() {
        // We explicitly delegate to the source we are adding meta data to.
        // This is because we don't expect to be called outside of the framework
        // implementations. So client's will talk about sources as their's which
        // we are wrapping.
        return underlyingSource.getUUID();
    }

    public String retrieve(String key) {
        return underlyingSource.retrieve(key);
    }

    public int compareTo(PrioritisedConfigurationSource that) {
        CompareToBuilder compareTo = new CompareToBuilder();
        compareTo.append(this.getPriority(), that.getPriority());
        return compareTo.toComparison();
    }

    public boolean isEncapsulatingWritable() {
        return underlyingSource instanceof WritableConfigurationSource;
    }

    public void store(String key, String value) {
        if (isEncapsulatingWritable()) {
            WritableConfigurationSource writableSource = (WritableConfigurationSource) this.underlyingSource;
            writableSource.store(key, value);
        }
    }

    boolean underlyingEquals(ConfigurationSource source) {
        EqualsBuilder eq = new EqualsBuilder();
        eq.append(underlyingSource, source);
        return eq.isEquals();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PrioritisedConfigurationSource that = (PrioritisedConfigurationSource) o;
        EqualsBuilder eqBuilder = new EqualsBuilder();
        eqBuilder.append(underlyingSource.getUUID(), that.underlyingSource.getUUID());
        return eqBuilder.isEquals();
    }

    @Override
    public int hashCode() {
        HashCodeBuilder hashBuilder = new HashCodeBuilder(17, 37);
        hashBuilder.append(priority);
        hashBuilder.append(underlyingSource);
        return hashBuilder.toHashCode();
    }
}
