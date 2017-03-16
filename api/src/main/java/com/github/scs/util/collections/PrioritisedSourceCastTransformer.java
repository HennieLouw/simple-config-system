package com.github.scs.util.collections;

import com.github.scs.api.ConfigurationSource;
import com.github.scs.impl.PrioritisedConfigurationSource;
import org.apache.commons.collections4.Transformer;

/**
 * Implementation of the Apache common collections {@link Transformer} to cast {@link ConfigurationSource} instances into {@link PrioritisedConfigurationSource}
 * instances.
 * <p>
 * Note, this transformer does not check if the given instances can be casted, and will throw a {@link ClassCastException} if given a non prioritised source.
 *
 * @author Hendrik Louw
 * @since 2017-03-16.
 */
public class PrioritisedSourceCastTransformer implements Transformer<ConfigurationSource, PrioritisedConfigurationSource> {

    public PrioritisedConfigurationSource transform(ConfigurationSource input) {
        return (PrioritisedConfigurationSource) input;
    }
}
