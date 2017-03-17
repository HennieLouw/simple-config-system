package com.github.scs.impl;

import com.github.scs.api.ConfigurationSource;
import com.github.scs.api.WritableConfigurationSource;
import com.github.scs.util.collections.WritableSourcePredicate;
import com.github.scs.util.collections.WritableSourceCastTransformer;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.collections4.functors.AndPredicate;
import org.apache.commons.collections4.functors.TruePredicate;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;

/**
 * Base implementation of a {@link ConfigurationSourcesWriter} which will update all {@link WritableConfigurationSource writable} source instances.
 *
 * @author Hendrik Louw
 * @since 2017-03-16.
 */
@SuppressWarnings("WeakerAccess")
public class SimpleSourcesWriter implements ConfigurationSourcesWriter {

    /** Global instance for ease of use. */
    @SuppressWarnings("WeakerAccess")
    public static final SimpleSourcesWriter INSTANCE = new SimpleSourcesWriter();

    /**
     * Defines the predicate which will be used to determine which of the sources in the client given list will be updated.
     * <p>
     * Note, it will not be necessary for the predicate to check for {@link WritableConfigurationSource writable } source instances, as this implementation
     * will perform such a check.
     *
     * @return The predicate which will be used, or {@code null} if no predicate required.
     */
    protected Predicate<ConfigurationSource> definePredicate() {
        return TruePredicate.truePredicate();
    }


    /**
     * Iterates the given collection of {@link ConfigurationSource sources} and collects all instance which are {@link WritableConfigurationSource writable}
     * and conforms to the criteria set out by the predicate from {@link #definePredicate()}
     *
     * @param sources The sources from which writable instances must be collected.
     * @return A collection of writable instances which can then be asked to {@link WritableConfigurationSource#store(String, String) store} the new key, value
     * configuration entry.
     *
     * @see #definePredicate()
     */
    protected Collection<WritableConfigurationSource> collectWritable(Collection<? extends ConfigurationSource> sources) {
        WritableSourcePredicate writable = WritableSourcePredicate.INSTANCE;
        Predicate<ConfigurationSource> validForUpdate = definePredicate();
        if (validForUpdate == null) {
            validForUpdate = TruePredicate.truePredicate();
        }

        AndPredicate<? super ConfigurationSource> writableAndValidForUpdate = new AndPredicate<ConfigurationSource>(writable, validForUpdate);
        Collection<ConfigurationSource> writableSources = CollectionUtils.select(sources, writableAndValidForUpdate);
        return CollectionUtils.collect(writableSources, WritableSourceCastTransformer.INSTANCE);
    }

    public void store(String key, String value, Collection<? extends ConfigurationSource> sources) {
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("Key of configuration entry is not allowed to be blank.");
        }

        Collection<WritableConfigurationSource> writableSources = collectWritable(sources);
        for (WritableConfigurationSource writableSource : writableSources) {
            writableSource.store(key, value);
        }
    }
}
